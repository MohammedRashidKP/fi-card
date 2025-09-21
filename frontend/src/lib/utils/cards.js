export function cardImagePath(card) {
  if (!card) return '';

  // Handle jokers
  if (card.isJoker) {
    return `/cards/${card.jokerColor.toLowerCase()}_joker.png`;
  }

  // Normal cards
  return `/cards/${card.rank.toLowerCase()}_of_${card.suit.toLowerCase()}.png`;
}
