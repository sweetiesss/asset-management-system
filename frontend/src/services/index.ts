import { authApi } from '@/configs/axios';
import { ExpectedResponse } from '@/types/dto';
import { Page } from '@/types/type';
import { AxiosRequestConfig } from 'axios';

/**
 * Generic function to update (patch) an entity via HTTP PATCH request.
 *
 * @template TResponse - The type of the response data
 * @template TRequest - The type of the request data
 * @param {string} path - The API endpoint path
 * @param {string} id - The ID of the entity to update
 * @param {TRequest} data - The data to send in the PATCH request
 * @param {string} [queryParms] - Optional query parameters to append to the URL
 * @returns {Promise<TResponse>} A promise that resolves to the response data
 * @throws {AxiosError} When the HTTP request fails
 */
export const patchEntity = async <TResponse, TRequest>(
  path: string,
  data: TRequest,
  id?: string,
  queryParms?: string
): Promise<ExpectedResponse<TResponse>> => {
  const query = queryParms ? `?${queryParms}` : '';
  const url = id ? `${path}/${id}${query}` : `${path}${query}`;
  const response = await authApi.patch<ExpectedResponse<TResponse>>(url, data);
  return response.data;
};

/**
 * Generic function to delete an entity via HTTP DELETE request.
 *
 * @param {string} path - The API endpoint path
 * @param {string} id - The ID of the entity to delete
 * @returns {Promise<ExpectedResponse<void>>} A promise that resolves when the deletion is complete
 * @throws {AxiosError} When the HTTP request fails
 */
export const deleteEntity = async (path: string, id: string): Promise<ExpectedResponse<void>> => {
  const response = await authApi.delete<ExpectedResponse<void>>(`${path}/${id}`);
  return response.data;
};

/**
 * Generic function to create a new entity via HTTP POST request.
 *
 * @template TResponse - The type of the response data
 * @template TRequest - The type of the request data
 * @param {string} path - The API endpoint path
 * @param {TRequest} data - The data to send in the POST request
 * @returns {Promise<TResponse>} A promise that resolves to the response data
 * @throws {AxiosError} When the HTTP request fails
 */
export const postEntity = async <TResponse, TRequest>(
  path: string,
  data: TRequest
): Promise<ExpectedResponse<TResponse>> => {
  const response = await authApi.post<ExpectedResponse<TResponse>>(path, data);
  return response.data;
};

/**
 * Generic function to retrieve a paginated list of entities via HTTP GET request.
 *
 * @template TResponse - The type of the response data
 * @template QueryParams - The type of the query parameters
 * @param {string} path - The API endpoint path
 * @param {QueryParams} params - The query parameters to send in the GET request
 * @returns {Promise<Page<TResponse>>} A promise that resolves to the paginated response data
 * @throws {AxiosError} When the HTTP request fails
 */
export const getEntities = async <TResponse, QueryParams>(path: string, params: QueryParams) => {
  const response = await authApi.get<ExpectedResponse<Page<TResponse>>>(path, { params });
  return response.data;
};

/**
 * Generic function to retrieve a single entity via HTTP GET request.
 *
 * @template TResponse - The type of the response data
 * @param {string} path - The API endpoint path
 * @returns {Promise<TResponse>} A promise that resolves to the response data
 * @throws {AxiosError} When the HTTP request fails
 */

export const getEntity = async <TResponse>(path: string) => {
  const response = await authApi.get<ExpectedResponse<TResponse>>(path);
  return response.data;
};

export const getEntityBlob = async <QueryParams = undefined>(
  path: string,
  params?: QueryParams
): Promise<{ fileName: string; blob: Blob }> => {
  const config: AxiosRequestConfig = {
    responseType: 'blob',
    ...(params && { params }),
  };

  const response = await authApi.get(path, config);
  const contentDisposition = response.headers['content-disposition'];
  const fileName = contentDisposition
    ? getFileNameFromHeader(contentDisposition)
    : 'downloaded-file';
  return {
    fileName: fileName,
    blob: new Blob([response.data], {
      type: response.headers['content-type'],
    }),
  };
};

function getFileNameFromHeader(disposition: string): string {
  const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
  const matches = filenameRegex.exec(disposition);
  if (matches != null && matches[1]) {
    return matches[1].replace(/['"]/g, ''); 
  }
  return 'downloaded-file'; 
}


export const putEntity = async <TResponse, TRequest>(endpoint: string, data: TRequest): Promise<ExpectedResponse<TResponse>> => {
  const response = await authApi.put<ExpectedResponse<TResponse>>(endpoint, data);
  return response.data;
}