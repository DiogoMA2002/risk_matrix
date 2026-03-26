export function formatCategoryName(rawEnum) {
  if (!rawEnum || typeof rawEnum !== 'string') return '';
  return rawEnum
    .replace(/_/g, ' ')
    .split(' ')
    .map(word => {
      if (!word) return '';
      return word.charAt(0).toLocaleUpperCase('pt-PT') +
        word.slice(1).toLocaleLowerCase('pt-PT');
    })
    .join(' ');
}
