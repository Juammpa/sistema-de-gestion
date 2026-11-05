package org.sistemagestion.backend.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sistemagestion.backend.config.CurrentOwnerResolver;
import org.sistemagestion.backend.model.Client;
import org.sistemagestion.backend.repository.ClientRepository;
import org.sistemagestion.backend.service.account.OwnerDataPurger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

// Prueba contra un PostgreSQL real (Testcontainers): Flyway crea el esquema verdadero, sin mocks.
// Verifica la garantia mas importante del sistema: cada usuario ve y borra solo lo suyo.
@DataJpaTest(properties = "spring.flyway.placeholders.initial_owner_id=00000000-0000-0000-0000-000000000001")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Testcontainers
@Import({CurrentOwnerResolver.class, OwnerDataPurger.class})
// Sin transaccion de test: cada llamada al repositorio abre su propia sesion, como en produccion.
// Con una transaccion envolviendo todoo, Hibernate reutilizaria la sesion del primer dueno.
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OwnerIsolationTest {

    private static final UUID OWNER_A = UUID.fromString("aaaaaaaa-0000-0000-0000-00000000000a");
    private static final UUID OWNER_B = UUID.fromString("bbbbbbbb-0000-0000-0000-00000000000b");

    // Tablas con owner_id propio
    private static final List<String> OWNED_TABLES = List.of(
            "client", "suppliers", "cut_meats", "butcher_shops", "current_accounts", "movements",
            "purchase_piece_weights", "purchase_types", "stock_cuts", "custom_payment_methods",
            "stock_piece_closures");

    // Subtipos de movements: no tienen owner_id, cuelgan del movimiento
    private static final List<String> MOVEMENT_SUBTYPES = List.of("payments", "sales", "purchases");

    static {
        // Igual que BackendApplication.main: en algunos Windows la zona por defecto llega como
        // "America/Buenos_Aires" (alias viejo) y PostgreSQL 17 la rechaza al conectar
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
    }

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private OwnerDataPurger purger;

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        // Deja la base vacia para el siguiente test (el contenedor es uno solo para toda la clase)
        jdbc.execute("truncate table " + String.join(", ", allTables()) + " restart identity cascade");
    }

    @Nested
    @DisplayName("Hibernate @TenantId")
    class TenantFilter {

        @Test
        void findAllOnlyReturnsRowsOfTheAuthenticatedOwner() {
            // Arrange: dos usuarios con datos, y se inicia sesion como el A
            seedOwner(OWNER_A);
            seedOwner(OWNER_B);
            authenticateAs(OWNER_A);

            // Act
            List<Client> clients = clientRepository.findAll();

            // Assert
            assertThat(clients).hasSize(1);
            assertThat(clients.getFirst().getOwnerId()).isEqualTo(OWNER_A);
        }

        @Test
        void findByIdCannotReachARowOfAnotherOwner() {
            // Arrange: el cliente existe, pero es del usuario B
            seedOwner(OWNER_A);
            Long clientIdOfB = seedOwner(OWNER_B);
            authenticateAs(OWNER_A);

            // Act: el usuario A pide el id del cliente de B
            var found = clientRepository.findById(clientIdOfB);

            // Assert
            assertThat(found).isEmpty();
        }

        @Test
        void savedRowsBelongToTheAuthenticatedOwner() {
            // Arrange
            authenticateAs(OWNER_A);
            Client newClient = Client.builder().name("Luis").lastName("Gomez").build();

            // Act: el dueno no se informa en el codigo, lo completa Hibernate
            Client saved = clientRepository.save(newClient);

            // Assert: se lee con SQL directo, sin pasar por el filtro de Hibernate
            UUID storedOwner = jdbc.queryForObject(
                    "select owner_id from client where id = ?", UUID.class, saved.getId());
            assertThat(storedOwner).isEqualTo(OWNER_A);
        }

        @Test
        void withoutAuthenticationNoRowIsVisible() {
            // Arrange: hay datos, pero nadie inicio sesion
            seedOwner(OWNER_A);

            // Act
            List<Client> clients = clientRepository.findAll();

            // Assert
            assertThat(clients).isEmpty();
        }
    }

    @Nested
    @DisplayName("OwnerDataPurger.purge()")
    class Purge {

        @Test
        void purgeRemovesEveryRowOfTheOwnerAndKeepsTheOthersIntact() {
            // Arrange: dos usuarios con exactamente los mismos datos (todas las tablas, incluidos
            // cheque rechazado y pagos que apuntan a otros pagos)
            seedOwner(OWNER_A);
            seedOwner(OWNER_B);
            Map<String, Integer> rowsOfB = countsOwnedBy(OWNER_B);

            // Act
            purger.purge(OWNER_A);

            // Assert: lo del usuario A desaparecio por completo...
            assertThat(countsOwnedBy(OWNER_A).values()).containsOnly(0);

            // ...y en las tablas queda exactamente lo del B: ni filas huerfanas ni filas de menos
            assertThat(totalCounts()).isEqualTo(rowsOfB);
        }

        @Test
        void purgeOfAnOwnerWithoutDataChangesNothing() {
            // Arrange
            seedOwner(OWNER_B);
            Map<String, Integer> before = totalCounts();

            // Act
            purger.purge(OWNER_A);

            // Assert
            assertThat(totalCounts()).isEqualTo(before);
        }
    }

    // ---------------------------------------------------------------- datos de prueba

    // Crea un usuario "completo": toca todas las tablas y todas las claves foraneas.
    // Devuelve el id de su cliente
    private Long seedOwner(UUID owner) {

        Long clientAccount = insert("insert into current_accounts (current_balance, owner_id) values (0, ?) returning id", owner);
        Long supplierAccount = insert("insert into current_accounts (current_balance, owner_id) values (0, ?) returning id", owner);

        Long client = insert("insert into client (name, last_name, owner_id) values ('Ana', 'Perez', ?) returning id", owner);
        insert("insert into butcher_shops (name, client_id, current_account_id, owner_id) values ('Sucursal', ?, ?, ?) returning id",
                client, clientAccount, owner);
        Long supplier = insert("insert into suppliers (company_name, current_account_id, supplier_type, owner_id) values ('Frigorifico', ?, 'BOVINE', ?) returning id",
                supplierAccount, owner);

        Long cutMeat = insert("insert into cut_meats (cut_meat_name, is_active, price_per_kilo, specie, owner_id) values ('Asado', true, 100, 'BOVINE', ?) returning id", owner);
        Long purchaseType = insert("insert into purchase_types (owner_id, specie, name) values (?, 'BOVINE', 'Novillo') returning id", owner);
        Long customMethod = insert("insert into custom_payment_methods (owner_id, name) values (?, 'IIBB') returning id", owner);

        // Una compra con su pieza, un corte sacado de esa pieza y la pieza cerrada
        Long purchase = movement(owner, supplierAccount);
        jdbc.update("insert into purchases (id, kilograms, price_per_kilo, specie, unit, purchase_type_id) values (?, 100, 10, 'BOVINE', 1, ?)",
                purchase, purchaseType);
        jdbc.update("insert into purchase_piece_weights (kilograms, piece_number, purchase_id, owner_id) values (100, 1, ?, ?)",
                purchase, owner);
        jdbc.update("insert into stock_piece_closures (owner_id, purchase_id, piece_number) values (?, ?, 1)", owner, purchase);

        // Una venta que consume ese corte
        Long sale = movement(owner, clientAccount);
        jdbc.update("insert into sales (id, client_id, cut_meat_id, cut_meat_name, kilograms, price_per_kilo, unit) values (?, ?, ?, 'Asado', 5, 100, 1)",
                sale, client, cutMeat);
        jdbc.update("insert into stock_cuts (owner_id, purchase_id, piece_number, cut_meat_id, kilograms, sale_id) values (?, ?, 1, ?, 5, ?)",
                owner, purchase, cutMeat, sale);

        // Pagos: un cheque, su rechazo (apunta al cheque), una transferencia, una transferencia a un
        // proveedor (apunta a la transferencia) y uno con metodo personalizado
        Long check = payment(owner, clientAccount, "CHECK");
        Long rejection = payment(owner, clientAccount, "CHECK_REJECTION");
        jdbc.update("update payments set rejected_payment_id = ? where id = ?", check, rejection);

        Long transfer = payment(owner, clientAccount, "TRANSFER");
        Long supplierTransfer = payment(owner, clientAccount, "SUPPLIER_TRANSFER");
        jdbc.update("update payments set supplier_id = ?, origin_payment_id = ? where id = ?", supplier, transfer, supplierTransfer);

        // La V9 exige informar el metodo al insertar un pago CUSTOM, por eso no usa payment()
        Long custom = movement(owner, clientAccount);
        jdbc.update("insert into payments (id, payment_type, custom_payment_method_id) values (?, 'CUSTOM', ?)", custom, customMethod);

        return client;
    }

    private Long movement(UUID owner, Long account) {
        return insert("insert into movements (amount, balance, date, current_account_id, owner_id) values (100, 100, current_date, ?, ?) returning id",
                account, owner);
    }

    private Long payment(UUID owner, Long account, String type) {
        Long id = movement(owner, account);
        jdbc.update("insert into payments (id, payment_type) values (?, ?)", id, type);
        return id;
    }

    private Long insert(String sql, Object... args) {
        return jdbc.queryForObject(sql, Long.class, args);
    }

    private void authenticateAs(UUID owner) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject(owner.toString()).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    // ---------------------------------------------------------------- conteos

    private List<String> allTables() {
        return Stream.concat(OWNED_TABLES.stream(), MOVEMENT_SUBTYPES.stream()).toList();
    }

    // Filas de un dueno en cada tabla (los subtipos se cuentan a traves de su movimiento)
    private Map<String, Integer> countsOwnedBy(UUID owner) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String table : allTables()) {
            String sql = MOVEMENT_SUBTYPES.contains(table)
                    ? "select count(*) from " + table + " where id in (select id from movements where owner_id = ?)"
                    : "select count(*) from " + table + " where owner_id = ?";
            counts.put(table, jdbc.queryForObject(sql, Integer.class, owner));
        }
        return counts;
    }

    // Filas totales de cada tabla, de todos los duenos
    private Map<String, Integer> totalCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String table : allTables()) {
            counts.put(table, jdbc.queryForObject("select count(*) from " + table, Integer.class));
        }
        return counts;
    }
}