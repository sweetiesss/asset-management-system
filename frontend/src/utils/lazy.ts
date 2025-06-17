import { lazy } from 'react';

export function lazyLoad(path: string, exportedItem?: string) {
  return lazy(() => {
    const promise = import(/* @vite-ignore */ `../${path}`);
    if (exportedItem) {
      return promise.then((module) => {
        return { default: module[exportedItem] };
      });
    } else {
      return promise;
    }
  });
}
