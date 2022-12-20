export class MenuAction {

  title: string = 'title';
  subTitle: string = 'subTitle';
  action: string = 'action';
  url: string = '';
  image: string = '';
  icon: string = '';
  bgColor: string = '';
  color: string = '';
  enabled: boolean = true;
  checked: boolean = false;

  constructor (_data?: any) {
    if (_data) {
      for (const key in _data) {
        if(this.hasOwnProperty(key)) {
          if(_data[key] !== null && _data[key] !== undefined) {
            switch (key) {
              default:
                (this as any)[key] = _data[key];
            }
          }
        }
      }
    }
  }
}
