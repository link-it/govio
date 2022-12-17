import { AfterContentChecked, Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { EventType } from 'projects/tools/src/lib/classes/events';
import { PageloaderService } from 'projects/tools/src/lib/pageloader.service';
import { AuthenticationService } from '../../services/authentication.service';

import { INavData } from '../../containers/gp-layout/gp-sidebar-nav';
import { navItemsMainMenu } from '../../containers/gp-layout/_nav';
import { GpSidebarNavHelper } from '../../containers/gp-layout/gp-sidebar-nav.helper';

@Component({
  selector: 'app-dashboard',
  templateUrl: 'dashboard.component.html',
  styleUrls: ['dashboard.component.scss']
})
export class DashboardComponent implements OnInit, AfterContentChecked {
  static readonly Name = 'DashboardComponent';

  config: any;
  appConfig: any;

  _spin: boolean = false;
  desktop: boolean = false;

  gridCols = 3;

  navItems: INavData[] = [];

  breadcrumbs: any[] = [
    { label: 'APP.TITLE.Dashboard', url: '', type: 'title', icon: 'dashboard' }
  ];

  single: any[] = [];
  multi: any[] = [];
  sparklineData: any[] = [];

  chartOptions: any = null;

  view: any = null; // [700, 400];

  _newDashboard: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private configService: ConfigService,
    public eventsManagerService: EventsManagerService,
    public pageloaderService: PageloaderService,
    public authenticationService: AuthenticationService,
    public sidebarNavHelper: GpSidebarNavHelper
  ) {
    this.appConfig = this.configService.getConfiguration();

    this.configService.getConfig('dashboard').subscribe(
      (config: any) => {
        this.config = config;
      }
    );

    this.navItems = [...navItemsMainMenu];
  }

  ngOnInit() {
    this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
      // language changed
    });

    this.pageloaderService.resetLoader();
    this.pageloaderService.isLoading.subscribe({
      next: (x) => { this._spin = x; },
      error: (e: any) => { console.log('loader error', e); }
    });
  }

  ngOnDestroy() {
  }

  ngAfterContentChecked(): void {
    this.desktop = (window.innerWidth >= 992);
  }

  _hasPermission(menu: any) {
    return this.authenticationService.hasPermission(menu.permission, 'view');
  }

  _dummyAction(event: any, param: any) {
    console.log(event, param);
  }

  onSelect(event: any) {
    // console.log(event);
  }
}
