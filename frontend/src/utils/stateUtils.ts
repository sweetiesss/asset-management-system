import { AssetStateHandler } from '@/types/asset';
import { ReturnStateHandler } from '@/types/assetReturn';
import { ReturntState } from '@/types/assignment';

export function getAssetStateLabel(value: string): string {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return new AssetStateHandler(value as any).label();
}

export function getAssetStateValue(label: string): string | undefined {
  return AssetStateHandler.getOptions('all').find((opt) => opt.label === label)?.value;
}

export const getReturnStateLabel = (value: string): string =>
  ReturnStateHandler.labelMap[value as ReturntState] ?? 'Unknown';

export const getReturnStateValue = (label: string): ReturntState | undefined =>
  Object.entries(ReturnStateHandler.labelMap).find(([, l]) => l === label)?.[0] as
    | ReturntState
    | undefined;
