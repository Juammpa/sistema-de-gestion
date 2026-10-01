import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { FlowChartCard } from '../../components/flow-chart-card/flow-chart-card';
import { KpiCard } from '../../components/kpi-card/kpi-card';
import { MonthlyComparisonCard } from '../../components/monthly-comparison-card/monthly-comparison-card';
import { MonthlySummaryCard } from '../../components/monthly-summary-card/monthly-summary-card';
import { RankingCard } from '../../components/ranking-card/ranking-card';
import { RecentMovementsCard } from '../../components/recent-movements-card/recent-movements-card';
import { DashboardService } from '../../services/dashboard.service';
import { monthShort, pctChange } from '../../utils/dashboard.utils';
import { ErrorState } from '../../../shared/components/error-state/error-state';

@Component({
  selector: 'app-dashboard-page',
  imports: [
    LoadingSpinner,
    KpiCard,
    FlowChartCard,
    MonthlySummaryCard,
    RankingCard,
    RecentMovementsCard,
    MonthlyComparisonCard,
    ErrorState,
  ],
  templateUrl: './dashboard-page.html',
  host: { class: 'block h-full min-h-0 flex-1 overflow-y-auto scrollbar-none' },
})
export default class DashboardPage {
  private dashboardService = inject(DashboardService);

  dashboard = rxResource({ stream: () => this.dashboardService.getDashboard() });

  // "28 de septiembre de 2026"
  today = new Intl.DateTimeFormat('es-AR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(new Date());

  // Datos derivados. Es null mientras carga o si fallo (.value() lanza error en ese caso)
  view = computed(() => {
    if (!this.dashboard.hasValue()) return null;

    const data = this.dashboard.value();
    const current = data.months[data.months.length - 1];
    const previous = data.months[data.months.length - 2];
    const net = data.receivable - data.payable;
    const previousNet = data.previousReceivable - data.previousPayable;

    return {
      data,
      current,
      net,
      monthLabel: monthShort(current.month),
      salesChange: pctChange(current.sales, previous.sales),
      receivableChange: pctChange(data.receivable, data.previousReceivable),
      payableChange: pctChange(data.payable, data.previousPayable),
      netChange: pctChange(net, previousNet),
    };
  });
}
