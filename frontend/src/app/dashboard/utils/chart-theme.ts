import { ChartOptions, Plugin } from 'chart.js';
import { formatCompact } from './dashboard.utils';

// Linea vertical que acompana al cursor en los graficos de linea
export const crosshairPlugin: Plugin = {
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
    ctx.strokeStyle = 'rgba(226, 232, 240, 0.7)';
    ctx.stroke();
    ctx.restore();
  },
};

// Opciones comunes de los graficos (paleta oscura, ejes en formato compacto)
export function baseChartOptions<T extends 'line' | 'bar'>(): ChartOptions<T> {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

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
        backgroundColor: '#0e1626',
        borderColor: '#334155',
        borderWidth: 1,
        titleColor: '#94a3b8',
        bodyColor: '#f1f5f9',
        padding: 10,
        callbacks: {
          label: (item) => `${item.dataset.label}: ${formatCompact(item.parsed.y ?? 0)}`,
        },
      },
    },
    scales: {
      x: { grid: { display: false }, border: { display: false }, ticks: { color: '#64748b' } },
      y: {
        beginAtZero: true,
        border: { display: false },
        grid: { color: 'rgba(148, 163, 184, 0.1)' },
        ticks: { color: '#64748b', callback: (value) => formatCompact(Number(value)) },
      },
    },
  };

  return options as unknown as ChartOptions<T>;
}
