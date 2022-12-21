import { Component, EventEmitter, HostListener, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';

import { TranslateService } from '@ngx-translate/core';
import moment from 'moment';

import { ConfigService } from 'projects/tools/src/lib/config.service';

declare const $: any;

@Component({
  selector: 'ui-search-bar-form',
  templateUrl: './search-bar-form.component.html',
  styleUrls: [
    './search-bar-form.component.scss'
  ]
})
export class SearchBarFormComponent implements OnInit, OnChanges {
  @Input() showHistory: boolean = true;
  @Input() historyStore: string = '';
  @Input() showSearch: boolean = true;
  @Input() searchFields: any[] = [];
  @Input() showSorting: boolean = true;
  @Input() showDataRange: boolean = false;
  @Input() sortField: string = '';
  @Input() sortDirection: string = 'asc';
  @Input() sortFields: any[] = [];
  @Input() classBlock: string = '';
  @Input() placeholder: string = 'Search or filter results...';
  @Input() formGroup: UntypedFormGroup = new UntypedFormGroup({});

  @Output() onSearch: EventEmitter<any> = new EventEmitter();
  @Output() onSort: EventEmitter<any> = new EventEmitter();

  _isOpen: boolean = false;
  _currentValues: any = {};

  _tokens: any[] = [];

  _placeholder: string = '';

  _history: any = {};
  _historyCount: number = 3;

  config: any;

  _clickInside: boolean = false;

  constructor(
    private matIconRegistry: MatIconRegistry,
    private domSanitizer: DomSanitizer,
    private translate: TranslateService,
    private configService: ConfigService
  ) {
    this.config = this.configService.getConfiguration();
    this._historyCount = this.config.AppConfig.Search.HistoryCount || this._historyCount;

    this._addIconsSvg();
  }

  ngOnInit() {
    this._history = this._getHistory();

    this.formGroup.valueChanges.subscribe(() => {
      this._tokens = this.__createTokens(this.formGroup.value);
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.placeholder) {
      this.placeholder = changes.placeholder.currentValue;
      this._placeholder = this.placeholder;
    }
  }

  @HostListener("click")
  clicked() {
    this._clickInside = true;
  }
  @HostListener("document:click")
  clickedOut() {
    if (!this._clickInside) {
      if (this._isOpen) {
        this._isOpen = false;
        $("#form_toggle").dropdown('hide');
        // this._onSearch();
      }
    }
    this._clickInside = false;
  }

  _addIconsSvg() {
    const _svgIcons = [
      { label: 'sort_ascending', icon: 'sort_ascending.svg' },
      { label: 'sort_descending', icon: 'sort_descending.svg' }
    ];
    _svgIcons.forEach(item => {
      this.matIconRegistry.addSvgIcon(
        item.label,
        this.domSanitizer.bypassSecurityTrustResourceUrl(`./assets/images/icons/${item.icon}`)
      );
    });
  }

  _selectSort(item: any) {
    this.sortField = item.field;
    this.onSort.emit({ sortField: this.sortField, sortBy: this.sortDirection });
  }

  _toggleSortBy() {
    this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    this.onSort.emit({ sortField: this.sortField, sortBy: this.sortDirection });
  }

  _isAscending() {
    return this.sortDirection === 'asc';
  }

  _getSortLabel(field: string) {
    return this.sortFields.find(item => item.field === field).label;
  }

  _onSearch(close: boolean = true, save: boolean = true) {
    // Get Form data
    const _oldValues = this._currentValues;
    this._currentValues = this.formGroup.value;
    this._tokens = this.__createTokens();
    if (_oldValues !== this._currentValues) {
      this.onSearch.emit(this._currentValues);
      if (!this.__isEmptyValues(this._currentValues) && save) {
        this._addHistory(this._currentValues);
        this._history = this._getHistory();
      }
    }
    if (this._tokens.length > 0) { this._placeholder = ''; }
    if (this._isOpen && close) {
      this._closeSearchDropDpwn(null);
    }
  }

  __isEmptyValues(values: any) {
    let _isEmpty = true;
    Object.keys(values).forEach(key => {
      if (values[key] !== '' && values[key] !== null) {
        _isEmpty = false;
        return false;
      }
      return true;
    });
    return _isEmpty;
  }

  __createTokens(values: any = null) {
    const _data: any = values || this._currentValues;
    const _tokens: any = [];
    Object.keys(_data).forEach(key => {
      if (_data[key] && _data[key] !== '') {
        const _spiltKey: string[] = [];
        key.split('.').map((item) => { _spiltKey.push(item[0].toUpperCase() + item.slice(1)) });
        const _key = _spiltKey.join('');
        const _label = this.translate.instant(`APP.LABEL.${_key}`);
        const _operator = this.__getOperator(key);
        const _value = this.__formatValue(key, _data[key]);
        _tokens.push({ key: key, label: _label, operator: _operator, value: _value });
      }
    });
    return _tokens;
  }

  __getOperator(key: string) {
    let _operator = '⊂';
    switch (key) {
      case 'creationDateFrom':
      case 'creationDateTo':
      case 'postingDateFrom':
      case 'postingDateTo':
      case 'status':
      case 'type':
        _operator = '=';
        break;
      default:
        break;
    }
    return _operator;
  }

  __formatValue(key: string, value: any) {
    let _value = value;
    switch (key) {
      case 'creationDateFrom':
      case 'creationDateTo':
      case 'postingDateFrom':
      case 'postingDateTo':
        _value = moment(value.valueOf()).format('DD/MM/YYYY');
        break;
      default:
        break;
    }
    return _value;
  }

  _clearToken(event: any, token: any, index: number) {
    event.stopPropagation();
    this._tokens.splice(index, 1);
    if (this._tokens.length === 0) {
      this._placeholder = this.placeholder;
    }
    this.formGroup.patchValue({
      [token.key]: ''
    });
    this._onSearch();
  }

  _restoreSearch(search: any) {
    const _valuesPatch: any = {};
    Object.keys(search).forEach(key => {
      _valuesPatch[key] = search[key];
    });
    this.formGroup.patchValue(_valuesPatch);
    this._onSearch(true, false);
  }

  _openSearch(event: any) {
    if (this._isOpen) {
      this._isOpen = false;
      $("#form_toggle").dropdown('hide');
    } else {
      this._isOpen = true;
      $("#form_toggle").dropdown('show');
    }
  }

  _clearSearch(event: any) {
    event.stopPropagation();
    this._tokens = [];
    this._currentValues = {};
    this.formGroup.reset();
    this._placeholder = this.placeholder;
    this._onSearch();
  }

  _closeSearchDropDpwn(event: any) {
    this._isOpen = false;
    $("#form_toggle").dropdown('hide');
  }

  // History

  _getHistory(): any {
    const _history: any = localStorage.getItem(`History_${this.historyStore}`);
    if (_history && _history !== 'null') {
      return this.__decodeDataOptions(_history);
    } else {
      return this.__setDefaultHistory();
    }
  }

  _getHistorytokens(values: any): any {
    return this.__createTokens(values);
  }

  _addHistory(data: any): any {
    let _history: any[] = this._getHistory();
    if (_history) {
      _history.push(data);
    } else {
      _history = [data];
    }
    if (_history.length > this._historyCount) {
      _history = _history.slice(_history.length - this._historyCount);
    }
    return this.__saveHistory(_history);
  }

  _clearHistory() {
    localStorage.removeItem(`History_${this.historyStore}`);
    this._history = this.__setDefaultHistory();
  }

  __setDefaultHistory(): any {
    this.__saveHistory([]);
    return this._getHistory();
  }

  __saveHistory(data: any): any {
    const lStorage = this.__encodeDataOptions(data);
    localStorage.setItem(`History_${this.historyStore}`, lStorage);
    return lStorage;
  }

  __decodeDataOptions(data: any): any {
    const decodeData = JSON.parse(decodeURI(window.atob(data)));
    return decodeData;
  }

  __encodeDataOptions(data: any): any {
    const encodeData = window.btoa(encodeURI(JSON.stringify(data)));
    return encodeData;
  }
}