declare module 'd3-org-chart' {
  export class OrgChart {
    container(el: HTMLElement | string): OrgChart
    data(data: any[]): OrgChart
    nodeWidth(fn: () => number): OrgChart
    nodeHeight(fn: () => number): OrgChart
    childrenMargin(fn: () => number): OrgChart
    compactMarginBetween(fn: () => number): OrgChart
    compactMarginPairing(fn: () => number): OrgChart
    neightbourMargin(fn: (a: any, b: any) => number): OrgChart
    buttonContent(fn: (data: any) => string): OrgChart
    nodeContent(fn: (data: any) => string): OrgChart
    onNodeClick(fn: (data: any) => void): OrgChart
    render(): OrgChart
  }
}
