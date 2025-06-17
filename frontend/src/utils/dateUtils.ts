/**
 * @description This file contains utility functions for date manipulation and validation.
 */

export const isDate = (date: Date): boolean => {
  return date instanceof Date && !isNaN(date.getTime());
};

export const calculateAge = (birthDate: Date): number => {
  const today = new Date();
  let age = today.getFullYear() - birthDate.getFullYear();
  const monthDiff = today.getMonth() - birthDate.getMonth();

  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age--;
  }

  return age;
};

export const isDateInFuture = (date: Date): boolean => {
  const today = new Date();
  return date > today;
};

export const formatDate = (dateString: string): string => {
  if (!dateString) return '';
  const [year, month, day] = dateString.split('-');
  return `${day}/${month}/${year}`;
};

export const isTodayOrFuture = (date: Date): boolean => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  date.setHours(0, 0, 0, 0);
  return date >= today;
};

export const today = (): string => {
  return new Date().toISOString().split('T')[0];
};

export const yesterday = (): string => {
  const date = new Date();
  date.setDate(date.getDate() - 1);
  return date.toISOString().split('T')[0];
};
