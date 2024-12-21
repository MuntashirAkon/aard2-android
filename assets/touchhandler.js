(function () {
    document.body.addEventListener("click", function (event) {
        var range = document.caretRangeFromPoint(event.clientX, event.clientY);
        if (
            range &&
            range.startContainer &&
            range.startContainer.nodeType === Node.TEXT_NODE
        ) {
            var text = range.startContainer.textContent;
            var start = range.startOffset;
            var end = range.startOffset;
            while (start > 0 && text[start - 1].match(/\w/)) start--;
            while (end < text.length && text[end].match(/\w/)) end++;
            var word = text.substring(start, end);

            if (word && word.length > 1) {
                $SLOB.onWordTapped(word);
                console.log('TAP WORD: ' + word);
            }
        }
    });
})();

