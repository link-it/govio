import { AfterViewInit, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'ui-item-type',
  templateUrl: './item-type.component.html',
  styleUrls: [
    './item-type.component.scss'
  ]
})
export class ItemTypeComponent implements OnInit, AfterViewInit {

  @Input('data') _data: any = null;
  @Input('config') _config: any = null;

  @Output() itemClick: EventEmitter<any> = new EventEmitter();

  constructor(
    private sanitized: DomSanitizer
  ) { }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
  }

  _sanitizeHtml(html: string) {
    return this.sanitized.bypassSecurityTrustHtml(html);
  }
}
