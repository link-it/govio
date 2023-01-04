import { INavData } from './gp-sidebar-nav';

export const navItemsMainMenu: INavData[] = [
  {
    title: true,
    label: 'APP.MENU.Dashboard',
    path: 'dashboard',
    url: '/dashboard',
    icon: 'dashboard',
    permission: 'DASHBOARD',
    attributes: { disabled: false }
  },
  {
    title: true,
    label: 'APP.MENU.Files',
    path: 'files',
    url: '/files',
    icon: 'topic',
    permission: 'FILES',
    attributes: { disabled: false }
  },
  {
    title: true,
    label: 'APP.MENU.Messages',
    path: 'messages',
    url: '/messages',
    icon: 'send',
    permission: 'MESSAGES',
    attributes: { disabled: false }
  }
];
