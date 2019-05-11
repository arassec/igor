export default {
  formatNameForSnackbar: function (name) {
    let parts = name.split(' ');
    for (let i = 0; i < parts.length; i++) {
      if (parts[i].length > 16) {
        parts[i] = name.substring(0, 16) + '...'
      }
    }
    return parts.join(' ');
  },
  formatNameForListEntry: function (name, maxLength) {
    if (name.length > maxLength) {
      return name.substring(0, maxLength) + '...'
    }
    return name
  }
}
