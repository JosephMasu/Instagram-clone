import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-avatar',
  template: `
    <div class="h-full w-full overflow-hidden rounded-full bg-zinc-200">
      @if (src()) {
        <img class="h-full w-full object-cover" [src]="src()!" [alt]="name()" />
      } @else {
        <span
          class="flex h-full w-full items-center justify-center font-semibold uppercase text-zinc-600"
          [style.font-size.px]="fontPx()"
        >
          {{ initial() }}
        </span>
      }
    </div>
  `,
  host: {
    class: 'inline-block shrink-0',
    '[style.width.px]': 'px()',
    '[style.height.px]': 'px()',
  },
})
export class Avatar {
  readonly src = input<string | null | undefined>(null);
  readonly name = input('user');
  readonly size = input<'xs' | 'sm' | 'md' | 'lg' | 'xl'>('md');

  readonly initial = computed(() => (this.name().trim().charAt(0) || '•').toUpperCase());

  readonly px = computed(() => {
    switch (this.size()) {
      case 'xs':
        return 24;
      case 'sm':
        return 32;
      case 'lg':
        return 77;
      case 'xl':
        return 150;
      default:
        return 40;
    }
  });

  readonly fontPx = computed(() => Math.round(this.px() * 0.36));
}
