import { describe, it, expect } from 'vitest';
import { getAssetStateLabel, getAssetStateValue, getReturnStateLabel, getReturnStateValue } from '../stateUtils';
import { AssetState } from '@/types/asset';
import { ReturntState } from '@/types/assignment';

describe('stateUtils', () => {
  describe('getAssetStateLabel', () => {
    it('should return correct label for AVAILABLE', () => {
      const result = getAssetStateLabel(AssetState.AVAILABLE);
      expect(result).toBe('Available');
    });

    it('should return correct label for NOT_AVAILABLE', () => {
      const result = getAssetStateLabel(AssetState.NOT_AVAILABLE);
      expect(result).toBe('Not available');
    });

    it('should return correct label for ASSIGNED', () => {
      const result = getAssetStateLabel(AssetState.ASSIGNED);
      expect(result).toBe('Assigned');
    });

    it('should return correct label for WAITING_FOR_RECYCLING', () => {
      const result = getAssetStateLabel(AssetState.WAITING_FOR_RECYCLING);
      expect(result).toBe('Waiting for recycling');
    });

    it('should return correct label for RECYCLED', () => {
      const result = getAssetStateLabel(AssetState.RECYCLED);
      expect(result).toBe('Recycled');
    });

    it('should handle unknown state gracefully', () => {
      const result = getAssetStateLabel('UNKNOWN_STATE');
      expect(result).toBe('Unknown State');
    });

    it('should handle empty string', () => {
      const result = getAssetStateLabel('');
      expect(result).toBe('Unknown State');
    });
  });

  describe('getAssetStateValue', () => {
    it('should return correct value for Available label', () => {
      const result = getAssetStateValue('Available');
      expect(result).toBe(AssetState.AVAILABLE);
    });

    it('should return correct value for Not available label', () => {
      const result = getAssetStateValue('Not available');
      expect(result).toBe(AssetState.NOT_AVAILABLE);
    });

    it('should return correct value for Assigned label', () => {
      const result = getAssetStateValue('Assigned');
      expect(result).toBe(AssetState.ASSIGNED);
    });

    it('should return correct value for Waiting for recycling label', () => {
      const result = getAssetStateValue('Waiting for recycling');
      expect(result).toBe(AssetState.WAITING_FOR_RECYCLING);
    });

    it('should return correct value for Recycled label', () => {
      const result = getAssetStateValue('Recycled');
      expect(result).toBe(AssetState.RECYCLED);
    });

    it('should handle unknown label gracefully', () => {
      const result = getAssetStateValue('Unknown Label');
      expect(result).toBeUndefined();
    });

    it('should handle empty string', () => {
      const result = getAssetStateValue('');
      expect(result).toBeUndefined();
    });
  });

  describe('getReturnStateLabel', () => {
    it('should return correct label for WAITING_FOR_RETURNING', () => {
      const result = getReturnStateLabel(ReturntState.WAITING_FOR_RETURNING);
      expect(result).toBe('Waiting for returning');
    });

    it('should return correct label for COMPLETED', () => {
      const result = getReturnStateLabel(ReturntState.COMPLETED);
      expect(result).toBe('Completed');
    });

    it('should return correct label for CANCELED', () => {
      const result = getReturnStateLabel(ReturntState.CANCELED);
      expect(result).toBe('Canceled');
    });

    it('should handle unknown state gracefully', () => {
      const result = getReturnStateLabel('UNKNOWN_STATE');
      expect(result).toBe('Unknown');
    });

    it('should handle empty string', () => {
      const result = getReturnStateLabel('');
      expect(result).toBe('Unknown');
    });
  });

  describe('getReturnStateValue', () => {
    it('should return correct value for Waiting for returning label', () => {
      const result = getReturnStateValue('Waiting for returning');
      expect(result).toBe(ReturntState.WAITING_FOR_RETURNING);
    });

    it('should return correct value for Completed label', () => {
      const result = getReturnStateValue('Completed');
      expect(result).toBe(ReturntState.COMPLETED);
    });

    it('should return correct value for Canceled label', () => {
      const result = getReturnStateValue('Canceled');
      expect(result).toBe(ReturntState.CANCELED);
    });

    it('should handle unknown label gracefully', () => {
      const result = getReturnStateValue('Unknown Label');
      expect(result).toBeUndefined();
    });

    it('should handle empty string', () => {
      const result = getReturnStateValue('');
      expect(result).toBeUndefined();
    });
  });

  describe('bidirectional mapping', () => {
    it('should correctly map asset state both ways', () => {
      const states = [
        AssetState.AVAILABLE,
        AssetState.NOT_AVAILABLE,
        AssetState.ASSIGNED,
        AssetState.WAITING_FOR_RECYCLING,
        AssetState.RECYCLED,
      ];

      states.forEach(state => {
        const label = getAssetStateLabel(state);
        const backToState = getAssetStateValue(label);
        expect(backToState).toBe(state);
      });
    });

    it('should correctly map return state both ways', () => {
      const states = [
        ReturntState.WAITING_FOR_RETURNING,
        ReturntState.COMPLETED,
        ReturntState.CANCELED,
      ];

      states.forEach(state => {
        const label = getReturnStateLabel(state);
        const backToState = getReturnStateValue(label);
        expect(backToState).toBe(state);
      });
    });
  });
});
