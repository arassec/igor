export default {
  formatNameForSnackbar: function (name) {
    let parts = name.split(' ');
    for (let i = 0; i < parts.length; i++) {
      if (parts[i].length > 16) {
        parts[i] = parts[i].substring(0, 16) + '...'
      }
    }
    return parts.join(' ');
  },
  shorten: function (input, maxLength) {
    if (input.length > maxLength) {
      return input.substring(0, maxLength) + '...'
    }
    return input
  },
  formatInstant: function (instant) {
    let options = {year: 'numeric', month: '2-digit', day: '2-digit'};
    let date = new Date(instant)
    return date.toLocaleTimeString(undefined, options)
  }
}
