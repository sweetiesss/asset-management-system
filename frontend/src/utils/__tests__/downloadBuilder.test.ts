import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { downloadBuilder } from '../downloadBuilder';

// Mock URL globalThis
globalThis.URL = globalThis.URL || {};

describe('downloadBuilder', () => {
  let mockLink: HTMLAnchorElement;

  beforeEach(() => {
    // Reset mocks
    mockLink = {
      href: '',
      download: '',
      click: vi.fn(),
      remove: vi.fn(),
    } as unknown as HTMLAnchorElement;

    // Mock document methods
    vi.spyOn(document, 'createElement').mockReturnValue(mockLink);
    vi.spyOn(document.body, 'appendChild').mockImplementation(() => mockLink);
    vi.spyOn(document.body, 'removeChild').mockImplementation(() => mockLink);
    
    // Mock URL.revokeObjectURL
    globalThis.URL.revokeObjectURL = vi.fn();
  });

  afterEach(() => {
    vi.clearAllMocks();
    vi.restoreAllMocks();
  });

  it('should create download link with blob URL', () => {
    const filename = 'test-file.txt';
    const blobUrl = 'blob:http://localhost:3000/test-url';

    downloadBuilder(filename, blobUrl);

    expect(document.createElement).toHaveBeenCalledWith('a');
    expect(mockLink.href).toBe(blobUrl);
    expect(mockLink.download).toBe(filename);
  });

  it('should trigger click and cleanup', () => {
    const filename = 'data.json';
    const blobUrl = 'blob:http://localhost:3000/data-url';

    downloadBuilder(filename, blobUrl);

    expect(mockLink.click).toHaveBeenCalled();
    expect(document.body.appendChild).toHaveBeenCalledWith(mockLink);
    expect(document.body.removeChild).toHaveBeenCalledWith(mockLink);
    expect(globalThis.URL.revokeObjectURL).toHaveBeenCalledWith(blobUrl);
  });

  it('should handle excel file download', () => {
    const filename = 'assets-report.xlsx';
    const blobUrl = 'blob:http://localhost:3000/excel-url';

    downloadBuilder(filename, blobUrl);

    expect(mockLink.download).toBe(filename);
    expect(mockLink.href).toBe(blobUrl);
    expect(mockLink.click).toHaveBeenCalled();
  });

  it('should handle CSV file download', () => {
    const filename = 'users.csv';
    const blobUrl = 'blob:http://localhost:3000/csv-url';

    downloadBuilder(filename, blobUrl);

    expect(mockLink.download).toBe(filename);
    expect(mockLink.href).toBe(blobUrl);
  });

  it('should handle filename with special characters', () => {
    const filename = 'report (2023-01-01) [final].txt';
    const blobUrl = 'blob:http://localhost:3000/special-url';

    downloadBuilder(filename, blobUrl);

    expect(mockLink.download).toBe(filename);
  });

  it('should handle PDF file download', () => {
    const filename = 'document.pdf';
    const blobUrl = 'blob:http://localhost:3000/pdf-url';

    downloadBuilder(filename, blobUrl);

    expect(mockLink.href).toBe(blobUrl);
    expect(mockLink.download).toBe(filename);
    expect(mockLink.click).toHaveBeenCalled();
  });

  it('should handle long filename', () => {
    const filename = 'very-long-filename-with-lots-of-characters-and-details-about-the-content.txt';
    const blobUrl = 'blob:http://localhost:3000/long-url';

    downloadBuilder(filename, blobUrl);

    expect(mockLink.download).toBe(filename);
  });

  it('should append and remove link from document body', () => {
    const filename = 'test.txt';
    const blobUrl = 'blob:http://localhost:3000/test-url';

    downloadBuilder(filename, blobUrl);

    expect(document.body.appendChild).toHaveBeenCalledWith(mockLink);
    expect(document.body.removeChild).toHaveBeenCalledWith(mockLink);
  });

  it('should cleanup blob URL after download', () => {
    const filename = 'cleanup-test.txt';
    const blobUrl = 'blob:http://localhost:3000/cleanup-url';

    downloadBuilder(filename, blobUrl);

    expect(globalThis.URL.revokeObjectURL).toHaveBeenCalledWith(blobUrl);
  });
});
