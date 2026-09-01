import { ListItem } from '../../shared/interfaces/list-item.interface';
import { Client } from '../interfaces/client.interface';

export class ClientMapper {
  // Client => ListItem
  static mapClientToListItem(client: Client): ListItem {
    return {
      id: client.id,
      title: `${client.lastName} ${client.name}`,
      subtitle: client.phoneNumber,
      initials: `${client.lastName[0]}${client.name[0]}`,
    };
  }

  // Client[] =>  ListItem[]
  static mapClientsToListItems(clients: Client[]): ListItem[] {
    return clients.map(this.mapClientToListItem);
  }
}
