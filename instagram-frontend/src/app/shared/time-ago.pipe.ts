import { Pipe, PipeTransform } from '@angular/core';
import { timeAgo } from './media';

@Pipe({ name: 'timeAgo' })
export class TimeAgoPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) {
      return '';
    }
    return timeAgo(value);
  }
}
