import { describe, it, expect, vi, beforeEach } from 'vitest';
import { AssetService } from '../AssetService';
import { authApi } from '@/configs/axios';
import { getEntities, getEntity, patchEntity, postEntity } from '../index';
import type { Asset, AssetListParams } from '@/types/asset';
import type { CreateAssetFormRequest } from '@/utils/form/schemas/create-asset-schema';
import type { EditAssetFormRequest } from '@/utils/form/schemas/edit-asset-schema';
import { AssetState } from '@/types/asset';
import { ExpectedResponse } from '@/types/dto';
import { Page } from '@/types/type';

// Mock all dependencies
vi.mock('@/configs/axios');
vi.mock('@/utils/api');
vi.mock('../index');

const mockAuthApi = vi.mocked(authApi);
const mockGetEntities = vi.mocked(getEntities);
const mockGetEntity = vi.mocked(getEntity);
const mockPatchEntity = vi.mocked(patchEntity);
const mockPostEntity = vi.mocked(postEntity);

// Properly type the mocked axios methods
mockAuthApi.get = vi.fn();
mockAuthApi.post = vi.fn();
mockAuthApi.put = vi.fn();
mockAuthApi.patch = vi.fn();
mockAuthApi.delete = vi.fn();

describe('AssetService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('createAsset', () => {
    it('should create an asset successfully', async () => {
      const mockData: CreateAssetFormRequest = {
        name: 'Test Asset',
        categoryId: 1,
        specification: 'Test specification',
        installedDate: '2023-01-01',
        state: AssetState.AVAILABLE,
      };

      const mockResponse = {
        success: true,
        data: { id: '1', ...mockData },
        message: 'Asset created successfully',
      };

      mockPostEntity.mockResolvedValue(mockResponse);

      const result = await AssetService.createAsset(mockData);

      expect(mockPostEntity).toHaveBeenCalledWith('assets', mockData);
      expect(result).toEqual(mockResponse);
    });

    it('should handle error during asset creation', async () => {
      const mockData: CreateAssetFormRequest = {
        name: 'Test Asset',
        categoryId: 1,
        specification: 'Test specification',
        installedDate: '2023-01-01',
        state: AssetState.AVAILABLE,
      };

      const mockError = new Error('Creation failed');
      mockPostEntity.mockRejectedValue(mockError);

      await expect(AssetService.createAsset(mockData)).rejects.toThrow('Creation failed');
      expect(mockPostEntity).toHaveBeenCalledWith('assets', mockData);
    });
  });


  describe('patch', () => {
    it('should patch asset successfully', async () => {
      const mockId = 'asset-123';
      const mockData: EditAssetFormRequest = {
        name: 'Updated Asset',
        specification: 'Updated specification',
        installedDate: '2023-02-01',
        state: AssetState.NOT_AVAILABLE,
        version: 1,
        categoryName: 'Test Category',
      };

      const expectedPostData = {
        name: mockData.name,
        specification: mockData.specification,
        installedDate: mockData.installedDate,
        state: mockData.state,
        version: mockData.version,
      };

      const mockResponse = {
        success: true,
        data: { id: mockId, ...expectedPostData },
        message: 'Asset updated successfully',
      };

      mockPatchEntity.mockResolvedValue(mockResponse);

      const result = await AssetService.patch(mockId, mockData);

      expect(mockPatchEntity).toHaveBeenCalledWith('assets', expectedPostData, mockId);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getAssets', () => {
    it('should get assets list successfully', async () => {
      const mockParams: AssetListParams = {
        page: 0,
        size: 20,
        search: 'test',
        sort: 'name',
        sortOrder: 'asc',
        states: [AssetState.AVAILABLE],
        categories: ['Electronics'],
      };

      const mockResponse: ExpectedResponse<Page<Asset<"basic">>> = {

        data: {
          content: [{ id: '1', name: 'Asset 1', category: { name: "Electronics", id: "ejdje", prefix: "EE12"}, state: AssetState.AVAILABLE, specification: 'Test spec', installedDate: '2023-01-01', version: 1, code: 'ASSET-001', location: { id: "1", name: "Ho Chi Minh", code: "HCM"} }],
          pageable: { pageNumber: 0, pageSize: 20, totalElements: 1, totalPages: 1, offset: 0, numberOfElements: 1, sorted: true, first: true, last: true, empty: false },
        },
        message: 'Success',
      };

      mockGetEntities.mockResolvedValue(mockResponse);

      const result = await AssetService.getAssets(mockParams);

      expect(mockGetEntities).toHaveBeenCalledWith('assets', mockParams);
      expect(result).toEqual(mockResponse);
    });
  });





  describe('getDetail', () => {
    it('should get asset detail successfully', async () => {
      const mockId = 'asset-123';
      const mockResponse = {
        success: true,
        data: { id: mockId, name: 'Test Asset' },
        message: 'Success',
      };

      mockGetEntity.mockResolvedValue(mockResponse);

      const result = await AssetService.getDetail(mockId);

      expect(mockGetEntity).toHaveBeenCalledWith(`assets/${mockId}`);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getBasic', () => {
    it('should get asset basic info successfully', async () => {
      const mockId = 'asset-123';
      const mockResponse = {
        success: true,
        data: { id: mockId, name: 'Test Asset' },
        message: 'Success',
      };

      mockGetEntity.mockResolvedValue(mockResponse);

      const result = await AssetService.getBasic(mockId);

      expect(mockGetEntity).toHaveBeenCalledWith(`assets/${mockId}`);
      expect(result).toEqual(mockResponse);
    });
  });
});
