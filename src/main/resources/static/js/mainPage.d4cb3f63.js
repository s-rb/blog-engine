(window["webpackJsonp"] = window["webpackJsonp"] || []).push([["mainPage"], {
    5872: function (t, e, a) {
        "use strict";
        var n = a("8abe"), s = a.n(n);
        s.a
    }, "6ccf": function (t, e, a) {
        "use strict";
        a.r(e);
        var n = function () {
            var t = this, e = t.$createElement, a = t._self._c || e;
            return a("main", {staticClass: "MainPage Wrapper"}, [a("Articles", {
                attrs: {
                    navItems: t.navItems,
                    className: "MainPage-Articles",
                    tagSelected: t.tagSelected,
                    postByDate: t.postByDate
                }
            }), a("Tags", {attrs: {className: "MainPage-Tags"}, on: {"select-tag": t.onClickTag}})], 1)
        }, s = [], i = (a("99af"), a("d3b7"), a("5530")), o = a("2f62"), c = function () {
            return a.e("articles").then(a.bind(null, "3a03"))
        }, r = function () {
            return a.e("tags").then(a.bind(null, "173f"))
        }, l = {
            name: "mainPage", components: {Articles: c, Tags: r}, data: function () {
                return {
                    navItems: [{name: "Новые", value: "recent"}, {
                        name: "Самые обсуждаемые",
                        value: "popular"
                    }, {name: "Лучшие", value: "best"}, {name: "Старые", value: "early"}], tagSelected: ""
                }
            }, computed: Object(i["a"])({}, Object(o["mapGetters"])(["blogInfo"]), {
                postByDate: function () {
                    return this.$route.params.date ? this.$route.params.date : ""
                }
            }), methods: {
                onClickTag: function (t) {
                    this.tagSelected = t, "/" !== this.$route.path && this.$router.push("/")
                }
            }, mounted: function () {
                this.$route.params.tag && (this.tagSelected = this.$route.params.tag)
            }, metaInfo: function () {
                return {title: this.blogInfo ? "".concat(this.blogInfo.title, " - ").concat(this.blogInfo.subtitle) : ""}
            }
        }, u = l, m = (a("5872"), a("2877")), g = Object(m["a"])(u, n, s, !1, null, null, null);
        e["default"] = g.exports
    }, "8abe": function (t, e, a) {
    }
}]);
//# sourceMappingURL=mainPage.d4cb3f63.js.map