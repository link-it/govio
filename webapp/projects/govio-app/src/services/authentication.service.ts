import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { EventType } from 'projects/tools/src/lib/classes/events';
import { ConfigService } from 'projects/tools/src/lib/config.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';

import * as _ from 'lodash';

export const AUTH_CONST: any = {
  storageSession: 'GOIO_SESSION'
};

export const PERMISSIONS: any = {
  govio_r: [
    { name: 'DASHBOARD', view: true, edit: false, create: false, delete: false },
    { name: 'FILES', view: true, edit: false, create: false, delete: false },
    { name: 'MESSAGES', view: true, edit: false, create: false, delete: false },
  ],
  govio_rw: [
    { name: 'DASHBOARD', view: true, edit: true, create: true, delete: true },
    { name: 'FILES', view: true, edit: true, create: true, delete: true },
    { name: 'MESSAGES', view: true, edit: true, create: true, delete: true },
  ],
  govio_adm: [
    { name: 'DASHBOARD', view: true, edit: true, create: true, delete: true },
    { name: 'FILES', view: true, edit: true, create: true, delete: true },
    { name: 'MESSAGES', view: true, edit: true, create: true, delete: true },
  ],
};

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private currentSession: any = null;

  config: any = null;
  appConfig: any = null;
  authDevelop: boolean = false;

  API_PROFILE: string = '/profile';
  API_LOGOUT: string = '/logout';

  constructor(
    private http: HttpClient,
    public configService: ConfigService,
    private eventsManagerService: EventsManagerService
  ) {
    this.config = this.configService.getConfiguration();
    this.appConfig = this.configService.getAppConfig();
    this.authDevelop = this.appConfig.AUTH_SETTINGS ? this.appConfig.AUTH_SETTINGS.DEVELOP : false;

    if (this.appConfig?.GOVAPI?.LOGOUT_URL) {
      this.API_LOGOUT = this.appConfig.GOVAPI.LOGOUT_URL;
    }

    this.reloadSession();
  }

  ngOnInit(): void {
  }

  login(username: string, password: string) {
    const _basiAuth = `${username}:${password}`;
    let httpHeaders = new HttpHeaders();
    httpHeaders = httpHeaders.set("Authorization", "Basic " + btoa(_basiAuth));

    const httpOptions = {
      headers: httpHeaders,
      withCredentials: true
    };

    let url = `${this.appConfig.GOVAPI['HOST']}${this.API_PROFILE}`;

    return this.http.get(url, httpOptions);
  }

  logout() {
    localStorage.removeItem(AUTH_CONST.storageSession);
    let url = `${this.appConfig.GOVAPI['HOST']}${this.API_LOGOUT}`;
    return this.http.get(url);
  }

  setCurrentSession(data: any) {
    const session = btoa(encodeURI(JSON.stringify(data)));
    localStorage.setItem(AUTH_CONST.storageSession, session);
  }

  getCurrentSession() {
    const storage = localStorage.getItem(AUTH_CONST.storageSession);
    if (storage) {
      const currentSession = JSON.parse(decodeURI(atob(storage)));
      return currentSession;
    }
    return null;
  }

  reloadSession() {
    this.currentSession = this.getCurrentSession();
    this.eventsManagerService.broadcast(EventType.SESSION_UPDATE, this.currentSession);
  }

  isLogged() {
    if (this.currentSession) {
      return true;
    }
    return this.authDevelop;
;
  }

  getUser() {
    const session = this.getCurrentSession();
    return session?.full_name ? session?.full_name : session?.principal ?? '<no-username>';
  }

  getRoles() {
    const session = this.getCurrentSession();
    return session?.roles ?? [];
  }

  hasRole(role: string) {
    const roles = this.getRoles();
    if (roles.findIndex((x: any) => x.name === role) > -1) {
      return true;
    }
    return false;
  }

  isAdmin() {
    if (!this.currentSession) {
      return false;
    } else {
      return (_.includes(this.currentSession.roles, 'govio_adm'));
    }
  }

  getPermissions() {
    const roles: any[] = this.getRoles();
    let permissions: any[] = [];
    roles.forEach((role: any) => {
      permissions = permissions.concat(PERMISSIONS[role]);
    });
    return permissions;
  }

  hasPermission(value: string, grant = 'view') {
    const uValue = value ? value.toUpperCase() : value;
    if (this.isAdmin() || uValue === 'PUBLIC') { return true; }
    const permissions = this.getPermissions();
    const idx = permissions.findIndex(o => o.name.toUpperCase() === uValue);
    const permission = (idx > -1) ? permissions[idx] : null;
    if (permission) {
      return permission[grant];
    }
    return false;
  }
}
