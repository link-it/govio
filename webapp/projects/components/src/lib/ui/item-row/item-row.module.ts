import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ItemRowComponent } from './item-row.component';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRippleModule } from '@angular/material/core';

import { ItemTypeModule } from './item-type/item-type.module';

@NgModule({
  declarations: [
    ItemRowComponent
  ],
  imports: [
    CommonModule,
    MatCheckboxModule,
    MatRippleModule,
    ItemTypeModule
  ],
  exports: [
    ItemRowComponent
  ]
})
export class ItemRowModule { }
