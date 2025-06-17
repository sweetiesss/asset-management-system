import { describe, it, expect } from 'vitest';
import { createAssetSchema, defaultValues, type CreateAssetFormRequest } from '../create-asset-schema';
import { AssetState } from '@/types/asset';

describe('create-asset-schema', () => {
  describe('defaultValues', () => {
    it('should have correct default values', () => {
      expect(defaultValues).toEqual({
        name: '',
        categoryId: 0,
        specification: '',
        installedDate: '',
        state: AssetState.AVAILABLE,
      });
    });
  });

  describe('createAssetSchema validation', () => {
    describe('name field', () => {
      it('should accept valid asset name', () => {
        const validData: CreateAssetFormRequest = {
          name: 'Valid Asset Name',
          categoryId: 1,
          specification: 'Valid specification',
          installedDate: '2023-01-01',
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(validData);
        expect(result.success).toBe(true);
      });

      it('should reject empty name', () => {
        const invalidData: CreateAssetFormRequest = {
          name: '',
          categoryId: 1,
          specification: 'Valid specification',
          installedDate: '2023-01-01',
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(invalidData);
        expect(result.success).toBe(false);
        if (!result.success) {
          expect(result.error.issues[0].path).toEqual(['name']);
          expect(result.error.issues[0].message).toBe('Asset name must be at least 1 character long');
        }
      });

      it('should accept name with whitespace', () => {
        const validData: CreateAssetFormRequest = {
          name: '   Valid Asset Name   ',
          categoryId: 1,
          specification: 'Valid specification',
          installedDate: '2023-01-01',
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(validData);
        expect(result.success).toBe(true);
      });

      it('should reject name that is too long', () => {
        const longName = 'A'.repeat(256); // Assuming max length is 255
        const invalidData: CreateAssetFormRequest = {
          name: longName,
          categoryId: 1,
          specification: 'Valid specification',
          installedDate: '2023-01-01',
          state: AssetState.AVAILABLE,
        };

        createAssetSchema.safeParse(invalidData);
      });
    });

    describe('categoryId field', () => {
      it('should accept valid category ID', () => {
        const validData: CreateAssetFormRequest = {
          name: 'Valid Asset Name',
          categoryId: 5,
          specification: 'Valid specification',
          installedDate: '2023-01-01',
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(validData);
        expect(result.success).toBe(true);
      });

      it('should reject category ID of 0', () => {
        const invalidData: CreateAssetFormRequest = {
          name: 'Valid Asset Name',
          categoryId: 0,
          specification: 'Valid specification',
          installedDate: '2023-01-01',
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(invalidData);
        expect(result.success).toBe(false);
        if (!result.success) {
          expect(result.error.issues[0].path).toEqual(['categoryId']);
          expect(result.error.issues[0].message).toBe('Category is required');
        }
      });

      it('should reject negative category ID', () => {
        const invalidData: CreateAssetFormRequest = {
          name: 'Valid Asset Name',
          categoryId: -1,
          specification: 'Valid specification',
          installedDate: '2023-01-01',
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(invalidData);
        expect(result.success).toBe(false);
        if (!result.success) {
          expect(result.error.issues[0].path).toEqual(['categoryId']);
          expect(result.error.issues[0].message).toBe('Category is required');
        }
      });
    });

    describe('specification field', () => {
      it('should accept valid specification', () => {
        const validData: CreateAssetFormRequest = {
          name: 'Valid Asset Name',
          categoryId: 1,
          specification: 'This is a valid specification with details about the asset.',
          installedDate: '2023-01-01',
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(validData);
        expect(result.success).toBe(true);
      });

      it('should reject empty specification', () => {
        const invalidData: CreateAssetFormRequest = {
          name: 'Valid Asset Name',
          categoryId: 1,
          specification: '',
          installedDate: '2023-01-01',
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(invalidData);
        expect(result.success).toBe(false);
        if (!result.success) {
          expect(result.error.issues[0].path).toEqual(['specification']);
          expect(result.error.issues[0].message).toBe('Specification is required');
        }
      });

      it('should accept specification with whitespace', () => {
        const validData: CreateAssetFormRequest = {
          name: 'Valid Asset Name',
          categoryId: 1,
          specification: '   Valid specification   ',
          installedDate: '2023-01-01',
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(validData);
        expect(result.success).toBe(true);
      });
    });

    describe('installedDate field', () => {
      it('should accept valid date', () => {
        const validData: CreateAssetFormRequest = {
          name: 'Valid Asset Name',
          categoryId: 1,
          specification: 'Valid specification',
          installedDate: '2023-01-01',
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(validData);
        expect(result.success).toBe(true);
      });

      it('should reject empty date', () => {
        const invalidData: CreateAssetFormRequest = {
          name: 'Valid Asset Name',
          categoryId: 1,
          specification: 'Valid specification',
          installedDate: '',
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(invalidData);
        expect(result.success).toBe(false);
        if (!result.success) {
          const dateError = result.error.issues.find(issue => issue.path[0] === 'installedDate');
          expect(dateError).toBeDefined();
        }
      });

      it('should accept future date', () => {
        const futureDate = new Date();
        futureDate.setFullYear(futureDate.getFullYear() + 1);
        const futureDateString = futureDate.toISOString().split('T')[0];

        const validData: CreateAssetFormRequest = {
          name: 'Valid Asset Name',
          categoryId: 1,
          specification: 'Valid specification',
          installedDate: futureDateString,
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(validData);
        expect(result.success).toBe(true);
      });

      it('should accept today\'s date', () => {
        const today = new Date().toISOString().split('T')[0];

        const validData: CreateAssetFormRequest = {
          name: 'Valid Asset Name',
          categoryId: 1,
          specification: 'Valid specification',
          installedDate: today,
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(validData);
        expect(result.success).toBe(true);
      });
    });

    describe('state field', () => {
      it('should accept AVAILABLE state', () => {
        const validData: CreateAssetFormRequest = {
          name: 'Valid Asset Name',
          categoryId: 1,
          specification: 'Valid specification',
          installedDate: '2023-01-01',
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(validData);
        expect(result.success).toBe(true);
      });

      it('should accept NOT_AVAILABLE state', () => {
        const validData: CreateAssetFormRequest = {
          name: 'Valid Asset Name',
          categoryId: 1,
          specification: 'Valid specification',
          installedDate: '2023-01-01',
          state: AssetState.NOT_AVAILABLE,
        };

        const result = createAssetSchema.safeParse(validData);
        expect(result.success).toBe(true);
      });

      it('should reject invalid state', () => {
        const invalidData = {
          name: 'Valid Asset Name',
          categoryId: 1,
          specification: 'Valid specification',
          installedDate: '2023-01-01',
          state: 'INVALID_STATE',
        };

        const result = createAssetSchema.safeParse(invalidData);
        expect(result.success).toBe(false);
        if (!result.success) {
          const stateError = result.error.issues.find(issue => issue.path[0] === 'state');
          expect(stateError).toBeDefined();
        }
      });
    });

    describe('complete form validation', () => {
      it('should validate complete valid form', () => {
        const validData: CreateAssetFormRequest = {
          name: 'Dell Laptop',
          categoryId: 1,
          specification: 'Dell Inspiron 15 3000, Intel Core i5, 8GB RAM, 256GB SSD',
          installedDate: '2023-01-15',
          state: AssetState.AVAILABLE,
        };

        const result = createAssetSchema.safeParse(validData);
        expect(result.success).toBe(true);
        if (result.success) {
          expect(result.data).toEqual(validData);
        }
      });

      it('should collect multiple validation errors', () => {
        const invalidData = {
          name: '',
          categoryId: 0,
          specification: '',
          installedDate: '',
          state: 'INVALID',
        };

        const result = createAssetSchema.safeParse(invalidData);
        expect(result.success).toBe(false);
        if (!result.success) {
          expect(result.error.issues.length).toBeGreaterThan(1);
          
          const fieldNames = result.error.issues.map(issue => issue.path[0]);
          expect(fieldNames).toContain('name');
          expect(fieldNames).toContain('categoryId');
          expect(fieldNames).toContain('specification');
        }
      });

      it('should handle missing fields', () => {
        const incompleteData = {
          name: 'Asset Name',
          // missing other required fields
        };

        const result = createAssetSchema.safeParse(incompleteData);
        expect(result.success).toBe(false);
        if (!result.success) {
          expect(result.error.issues.length).toBeGreaterThan(1);
        }
      });
    });
  });
});
