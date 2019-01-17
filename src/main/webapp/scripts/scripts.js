/*


 */

var balanced = function (a) {
    function b(d) {
        if (c[d]) return c[d].exports;
        var e = c[d] = {
            exports: {},
            id: d,
            loaded: !1
        };
        return a[d].call(e.exports, e, e.exports, b), e.loaded = !0, e.exports
    }

    var c = {};
    return b.m = a, b.c = c, b.p = "", b(0)
}([function (a, b, c) {
    function d(a) {
        if (a = a || {}, !a.open) throw new Error('Balanced: please provide a "open" property');
        if (!a.close) throw new Error('Balanced: please provide a "close" property');
        if (this.balance = a.balance || !1, this.exceptions = a.exceptions || !1, this.caseInsensitive = a.caseInsensitive, this.head = a.head || a.open, this.head = Array.isArray(this.head) ? this.head : [this.head], this.open = Array.isArray(a.open) ? a.open : [a.open], this.close = Array.isArray(a.close) ? a.close : [a.close], !Array.isArray(this.head) || !Array.isArray(this.open) || !Array.isArray(this.close) || this.head.length !== this.open.length || this.open.length !== this.close.length) throw new Error('Balanced: if you use arrays for a "head,open,close" you must use matching arrays for all options');
        var b = k(this.head.map(this.regExpFromArrayGroupedMap, this)),
            c = k(this.open.map(this.regExpFromArrayGroupedMap, this)),
            d = k(this.close.map(this.regExpFromArrayGroupedMap, this));
        this.regExp = k([b, c, d], "g" + (this.caseInsensitive ? "i" : "")), this.regExpGroupLength = this.head.length
    }

    function e(a, b, c) {
        return a.toString().length < b ? e(c + a, b) : a
    }

    function f(a, b, c) {
        for (var d = h(b.substr(0, c + 1), /^.*\n?$/gim), f = h(b, /^.*\n?$/gim), g = d.length - 1, i = d.length ? d[d.length - 1].index : 0, j = c + 1 - i, k = "", l = 2, m = String(d.length + Math.min(f.length - d.length, l)).length, n = l; n >= 0; n--) g - n >= 0 && f[g - n] && (k += e(g - n + 1, m, " ") + ": " + b.substr(f[g - n].index, f[g - n].length).replace(/\n/g, "") + "\n");
        for (n = 0; j - 1 + (m + 2) > n; n++) k += "-";
        for (k += "^\n", n = 1; l >= n; n++) g + n >= 0 && f[g + n] && (k += e(g + n + 1, m, " ") + ": " + b.substr(f[g + n].index, f[g + n].length).replace(/\n/g, "") + "\n");
        k = k.replace(/\t/g, " ").replace(/\n$/, "");
        var o = new Error(a + " at " + (g + 1) + ":" + j + "\n\n" + k);
        return o.line = g + 1, o.column = j, o.index = c, o
    }

    function g(a, b) {
        return a >= b.index && a <= b.index + b.length - 1
    }

    function h(a, b) {
        var c, d = new RegExp(b),
            e = [];
        if (a)
            for (; c = d.exec(a);) e.push({
                index: c.index,
                length: c[0].length,
                match: c[0]
            }), c[0].length || d.lastIndex++;
        return e
    }

    function i(a, b, c) {
        var d = 0;
        if (!a) return b;
        for (var e = 0; e < a.length; e++) {
            var f = a[e],
                g = String(c(b.substr(f.index + d + f.head.length, f.length - f.head.length - f.tail.length), f.head, f.tail));
            b = b.substr(0, f.index + d) + g + b.substr(f.index + d + f.length, b.length - (f.index + d + f.length)), d += g.length - f.length
        }
        return b
    }

    function j(a) {
        return a.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&")
    }

    function k(a, b, c) {
        var d = a.map(function (a) {
            return a instanceof RegExp ? a.source : j(a)
        }, this).join("|");
        return d = c ? "(" + d + ")" : "(?:" + d + ")", new RegExp(d, b || void 0)
    }

    function l(a, b) {
        return a.filter(function (a) {
            for (var c = !1, d = 0; d < b.length; d++)
                if (g(a.index, b[d])) {
                    c = !0;
                    break
                }
            return !c
        })
    }

    d.prototype = {
        regExpFromArrayGroupedMap: function (a) {
            return k([a], null, !0)
        },
        matchContentsInBetweenBrackets: function (a, b) {
            for (var c, d = new RegExp(this.regExp), e = [], h = [], i = null, j = !0; c = d.exec(a);) {
                if (b) {
                    for (var k = !1, l = 0; l < b.length; l++) g(c.index, b[l]) && (k = !0);
                    if (k) continue
                }
                var m = c.indexOf(c[0], 1) - 1,
                    n = Math.floor(m / this.regExpGroupLength),
                    o = m - Math.floor(m / this.regExpGroupLength) * this.regExpGroupLength;
                if (i || 0 !== n || this.balance && (!this.balance || e.length)) {
                    if (1 === n || 0 === n) e.push(o);
                    else if (2 === n) {
                        var p = e.pop();
                        if (p === o) null !== i && 0 === e.length && (h.push({
                            index: i.index,
                            length: c.index + c[0].length - i.index,
                            head: i[0],
                            tail: c[0]
                        }), i = null);
                        else if (this.balance && (j = !1, this.exceptions)) {
                            if (void 0 === p) throw f("Balanced: unexpected close bracket", a, c.index);
                            if (p !== o) throw f('Balanced: mismatching close bracket, expected "' + this.close[p] + '" but found "' + this.close[o] + '"', a, c.index)
                        }
                    }
                } else i = c, this.balance ? e.push(o) : e = [o]
            }
            if (this.balance) {
                if (this.exceptions && (!j || 0 !== e.length)) throw f('Balanced: expected "' + this.close[e[0]] + '" close bracket', a, a.length);
                return j && 0 === e.length ? h : null
            }
            return h
        },
        replaceMatchesInBetweenBrackets: function (a, b, c) {
            var d = this.matchContentsInBetweenBrackets(a, c);
            return i(d, a, b)
        }
    }, b.replaceMatchesInString = i, b.getRangesForMatch = h, b.isIndexInRange = g, b.rangesWithout = l, b.Balanced = d, b.replacements = function (a) {
        a = a || {};
        var b = new d({
            head: a.head,
            open: a.open,
            close: a.close,
            balance: a.balance,
            exceptions: a.exceptions,
            caseInsensitive: a.caseInsensitive
        });
        if (!a.source) throw new Error('Balanced: please provide a "source" property');
        if ("function" != typeof a.replace) throw new Error('Balanced: please provide a "replace" function');
        return b.replaceMatchesInBetweenBrackets(a.source, a.replace)
    }, b.matches = function (a) {
        var b = new d({
            head: a.head,
            open: a.open,
            close: a.close,
            balance: a.balance,
            exceptions: a.exceptions,
            caseInsensitive: a.caseInsensitive
        });
        if (!a.source) throw new Error('Balanced: please provide a "source" property');
        return b.matchContentsInBetweenBrackets(a.source, a.ignore)
    }
}]);