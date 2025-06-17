export const TIME = {
  TOAST_DURATION: 5000,
};

export const RANGE = {
  FIRST_NAME: {
    MIN: 1,
    MAX: 25,
  },
  LAST_NAME: {
    MIN: 1,
    MAX: 50,
  },
  DOB: {
    MIN_AGE: 18,
  },
  USERNAME: {
    MIN: 3,
    MAX: 50,
  },
  PASSWORD: {
    MIN: 8,
    MAX: 50,
  },
  ASSET_NAME: {
    MIN: 1,
    MAX: 255,
  },
  ASSET_SPECIFICATION: {
    MIN: 1,
    MAX: 2000,
  },
  CATEGORY_NAME: {
    MIN: 2,
    MAX: 50,
  },
  CATEGORY_PREFIX: {
    LENGTH: 2,
  },
  NOTE: {
    MAX: 500,
  },
};

export const MESSAGE = {
  FIRST_NAME: {
    REQUIRED: 'First name is required',
    MAX_LENGTH: 'First name must be less than 128 characters',
    INVALID_FORMAT: 'First name must contain only letters and spaces',
  },
  LAST_NAME: {
    REQUIRED: 'Last name is required',
    MAX_LENGTH: 'Last name must be less than 128 characters',
    INVALID_FORMAT: 'Last name must contain only letters and spaces',
  },
  GENDER: {
    INVALID: 'Please select gender',
  },
  USER_TYPE: {
    INVALID: 'Please select a user type',
  },
  DOB: {
    INVALID: 'Please enter a valid date',
    FUTURE: 'Date of birth cannot be in the future',
    UNDERAGE: 'User is under 18. Please select a different date',
  },
  JOINED_AT: {
    INVALID: 'Please enter a valid date',
    FUTURE: 'Joined date cannot be in the future',
    WEEKEND: 'Joined date is Saturday or Sunday. Please select a different date',
    JOINED_BEFORE_DOB:
      'Joined date is not later than Date of Birth. Please select a different date',
  },
  LOCATION: {
    REQUIRED_FOR_ADMIN: 'Location is required for admin',
  },
  USERNAME: {
    TOO_SHORT: 'Username must be at least 3 characters long',
    TOO_LONG: 'Username must be at most 50 characters long',
  },
  PASSWORD: {
    WEAK: 'Password must be 8–50 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&#)',
  },
  ASSET_NAME: {
    REQUIRED: 'Asset name is required',
    TOO_SHORT: 'Asset name must be at least 1 character long',
    TOO_LONG: 'Asset name must be at most 255 characters long',
    INVALID_FORMAT: 'Asset name must contain only letters, numbers, and spaces',
  },
  CATEGORY_NAME: {
    REQUIRED: 'Category name is required',
    TOO_SHORT: 'Category name must be at least 2 characters long',
    TOO_LONG: 'Category name must be at most 50 characters long',
    INVALID_FORMAT: 'Category name must contain only letters, numbers, and spaces',
  },
  CATEGORY_PREFIX: {
    REQUIRED: 'Category prefix is required',
    LENGTH: 'Category prefix must be exactly 2 characters long',
    INVALID_FORMAT: 'Category prefix must contain only uppercase letters',
  },
  NOTE: {
    TOO_LONG: 'Note must not exceed 500 characters',
  },
  ASSIGNED_DATE: {
    INVALID: 'Please enter a valid date',
    FUTURE: 'Assigned Date cannot be in the past.',
    BEFORE_OLD_DATE:
      'The updated Assigned Date must not be earlier than the original Assigned Date if it is in the past',
  },
  UUID: {
    REQUIRED: 'ID is required',
    INVALID_TYPE: 'ID must be a string',
    INVALID_FORMAT: 'Invalid ID format',
  },
  REPORT_FILENAME: {
    INVALID: 'Please enter a valid file name',
    TOO_LONG: 'File name must be at most 100 characters long',
  },
  REPORT_START_DATE: {
    INVALID: 'Please enter a valid start date',
    FUTURE: 'Start date cannot be in the future',
  },
  REPORT_END_DATE: {
    INVALID: 'Please enter a valid end date',
    FUTURE: 'End date cannot be in the future',
    BEFORE_START_DATE: 'End date must be later than Start date',
  },
};

export const ERROR = {
  PASSWORD_DOES_NOT_MATCH: {
    CODE: 'OLD_PASSWORD_NOT_MATCH',
  },
  NEW_PASSWORD_MUST_BE_DIFFERENT: {
    CODE: 'NEW_PASSWORD_MUST_BE_DIFFERENT',
  },
  CATEGORY_NAME_ALREADY_EXISTS: {
    CODE: 'CATEGORY_NAME_ALREADY_EXISTS',
    MESSAGE: 'Category is already existed. Please enter a different category',
  },
  CATEGORY_PREFIX_ALREADY_EXISTS: {
    CODE: 'CATEGORY_PREFIX_ALREADY_EXISTS',
    MESSAGE: 'Prefix is already existed. Please enter a different prefix',
  },
};

export const PARAM_KEYS = {
  PAGE: 'page',
  SIZE: 'size',
  SEARCH: 'search',
  SORT: 'sort',
  SORT_ORDER: 'sortOrder',
  ROLES: 'roles',
  MESSAGE: 'message',
  MESSAGE_TYPE: 'messageType',
  STATES: 'states',
  CATEGORIES: 'categories',
  DATES: 'assignedDate',
};

export const NAVIGATOR: { pattern: RegExp; label: string }[] = [
  { pattern: /^\/$/, label: 'Home' },
  { pattern: /^\/users$/, label: 'Manage User' },
  { pattern: /^\/users\/create$/, label: 'Manage User > Create New User' },
  { pattern: /^\/users\/[^/]+\/edit$/, label: 'Manage User > Edit User' },

  { pattern: /^\/assets$/, label: 'Manage Asset' },
  { pattern: /^\/assets\/create$/, label: 'Manage Asset > Create New Asset' },
  { pattern: /^\/assets\/[^/]+\/edit$/, label: 'Manage Asset > Edit Asset' },

  { pattern: /^\/assignments$/, label: 'Manage Assignment' },
  { pattern: /^\/assignments\/create$/, label: 'Manage Assignment > Create New Assignment' },
  { pattern: /^\/assignments\/[^/]+\/edit$/, label: 'Manage Assignment > Edit Assignment' },

  { pattern: /^\/returning-requests$/, label: 'Request for Returning' },
  { pattern: /^\/reports$/, label: 'Report' },
  { pattern: /^\/reports\/create$/, label: 'Report > Create Detailed Report' },
];
export const ROLE_OPTIONS = ['Admin', 'Staff'];

export const DEFAULT_SORT_ORDER: 'asc' | 'desc' = 'asc';

export const STATE_OPTIONS = [
  'ASSIGNED',
  'AVAILABLE',
  'NOT_AVAILABLE',
  'WAITING_FOR_RECYCLING',
  'RECYCLED',
];
