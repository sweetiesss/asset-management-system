function capitalizeWord(word: string): string {
  return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
}
export default function capitalizeEachWord(input: string): string {
  return input
    .trim()
    .split(/\s+/)
    .map((word) => capitalizeWord(word))
    .join(' ');
}