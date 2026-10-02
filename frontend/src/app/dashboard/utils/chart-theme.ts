import { ChartOptions, Plugin } from 'chart.js';
import { ResolvedTheme } from '../../core/theme/theme.service';
import { formatCompact } from './dashboard.utils';

// Colores de los gráficos según el tema
const PALETTE = {
  dark: {
    tooltipBg: '#0e1626',
    tooltipBorder: '#334155',
    tooltipTitle: '#94a3b8',
    tooltipBody: '#f1f5f9',
    tick: '#64748b',
    grid: 'rgba(148, 163, 184, 0.1)',
    crosshair: 'rgba(226, 232, 240, 0.7)',
  },
  light: {
    tooltipBg: '#ffffff',
    tooltipBorder: '#cbd5e1',
    tooltipTitle: '#64748b',
    tooltipBody: '#0f172a',
    tick: '#64748b',
    grid: 'rgba(100, 116, 139, 0.15)',
    crosshair: 'rgba(15, 23, 42, 0.35)',
  },
} as const;

// Linea vertical que acompana al cursor en los graficos de linea
export const crosshairPlugin = (theme: ResolvedTheme): Plugin => ({
  id: 'crosshair',
  afterDatasetsDraw(chart) {
    const active = chart.tooltip?.getActiveElements();
    if (!active?.length) return;

    const { ctx, chartArea } = chart;
    const x = active[0].element.x;

    ctx.save();
    ctx.beginPath();
    ctx.moveTo(x, chartArea.top);
    ctx.lineTo(x, chartArea.bottom);
    ctx.lineWidth = 1;
    ctx.strokeStyle = PALETTE[theme].crosshair;
    ctx.stroke();
    ctx.restore();
  },
});

// Opciones comunes de los graficos (ejes en formato compacto)
export function baseChartOptions<T extends 'line' | 'bar'>(theme: ResolvedTheme): ChartOptions<T> {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const colors = PALETTE[theme];

  // Se tipa como 'line' (usa los mismos ejes y tooltip que 'bar') para que los callbacks
  // tengan tipos concretos. Se adapta a T recien al retornar.
  const options: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    animation: reduceMotion ? false : { duration: 500 },
    interaction: { mode: 'index', intersect: false },
    plugins: {
      legend: { display: false }, // la leyenda se dibuja en HTML
      tooltip: {
        backgroundColor: colors.tooltipBg,
        borderColor: colors.tooltipBorder,
        borderWidth: 1,
        titleColor: colors.tooltipTitle,
        bodyColor: colors.tooltipBody,
        padding: 10,
        callbacks: {
          label: (item) => `${item.dataset.label}: ${formatCompact(item.parsed.y ?? 0)}`,
        },
      },
    },
    scales: {
      x: { grid: { display: false }, border: { display: false }, ticks: { color: colors.tick } },
      y: {
        beginAtZero: true,
        border: { display: false },
        grid: { color: colors.grid },
        ticks: { color: colors.tick, callback: (value) => formatCompact(Number(value)) },
      },
    },
  };

  return options as unknown as ChartOptions<T>;
}
