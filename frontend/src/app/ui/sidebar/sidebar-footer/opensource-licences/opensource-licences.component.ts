import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-opensource-licences',
    templateUrl: './opensource-licences.component.html',
    styleUrls: ['./opensource-licences.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [TranslatePipe]
})
export class OpensourceLicencesComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
