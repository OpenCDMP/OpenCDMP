import {TransitionGroupItemDirective} from "./transition-group-item.directive";
import {AfterViewInit, Component, ContentChildren, Input, OnDestroy, QueryList} from "@angular/core";
import {Subscription} from "rxjs";
import Timeout = NodeJS.Timeout;

@Component({
    selector: '[transition-group]',
    template: '<ng-content></ng-content>',
    standalone: false
})
export class TransitionGroupComponent implements AfterViewInit, OnDestroy {
  @ContentChildren(TransitionGroupItemDirective) items: QueryList<TransitionGroupItemDirective>;
  private subscription: Subscription;
  private timeout: Timeout;

  ngAfterViewInit() {
    this.init();
    this.subscription = this.items.changes.subscribe(items => {
      items.forEach(item => item.prevPos = item.newPos || item.prevPos);
      items.forEach(this.runCallback);
      this.refreshPosition('newPos');
      items.forEach(item => item.prevPos = item.prevPos || item.newPos); // for new items

      const animate = () => {
        items.forEach(this.applyTranslation);
        this['_forceReflow'] = document.body.offsetHeight; // force reflow to put everything in position
        this.items.forEach(this.runTransition.bind(this));
      }

      const willMoveSome = items.some((item) => {
        const dx = item.prevPos.left - item.newPos.left;
        const dy = item.prevPos.top - item.newPos.top;
        return dx || dy;
      });

      if (willMoveSome) {
        animate();
      } else {
        setTimeout(() => { // for removed items
          this.refreshPosition('newPos');
          animate();
        }, 0);
      }
    });
  }

  ngOnDestroy() {
    this.clear();
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  clear() {
    if (this.timeout) {
      clearTimeout(this.timeout);
    }
  }

  init() {
    this.clear();
    this.refreshPosition('prevPos');
    this.refreshPosition('newPos');
  }

  runCallback(item: TransitionGroupItemDirective) {
    if (item.moveCallback) {
      item.moveCallback();
    }
  }

  runTransition(item: TransitionGroupItemDirective) {
    if (!item.moved) {
      return;
    }
    const cssClass = 'list-move';
    let el = item.el;
    let style: any = el.style;
    el.classList.add(cssClass);
    style.transform = style.WebkitTransform = style.transitionDuration = '';
    el.addEventListener('transitionend', item.moveCallback = (e: any) => {
      if (!e || /transform$/.test(e.propertyName)) {
        el.removeEventListener('transitionend', item.moveCallback);
        item.moveCallback = null;
        el.classList.remove(cssClass);
      }
    });
  }

  refreshPosition(prop: string) {
    this.items.forEach(item => {
      item[prop] = item.el.getBoundingClientRect();
    });
  }

  applyTranslation(item: TransitionGroupItemDirective) {
    item.moved = false;
    const dx = item.prevPos.left - item.newPos.left;
    const dy = item.prevPos.top - item.newPos.top;
    if (dx || dy) {
      item.moved = true;
      let style: any = item.el.style;
      style.transform = style.WebkitTransform = 'translate(' + dx + 'px,' + dy + 'px)';
      style.transitionDuration = '0s';
    }
  }
}
