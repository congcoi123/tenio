//Author: Monie Corleone
//Purpose: To draw line chart in canvas element
//The MIT License (MIT)
//Copyright (c) <2015> <Monie Corleone>
; (function ($, window, document, undefined) {
    var pluginName = "SimpleChart";
    var defaults = {
        ChartType: "Line", //Area, Scattered, Bar, Hybrid, Pie, Stacked, Stacked Hybrid
        xPadding: 60,
        yPadding: 50,
        topmargin: 25,
        rightmargin: 20,
        data: null,
        toolwidth: 300,
        toolheight: 300,
        axiscolor: "#333",
        font: "italic 10pt sans-serif",
        headerfontsize: "14px",
        axisfontsize: "12px",
        piefontsize: "13px",
        pielabelcolor: "#fff",
        pielabelpercentcolor: "#000",
        textAlign: "center",
        textcolor: "#E6E6E6",
        showlegends: true,
        showpielables: false,
        legendposition: 'bottom',
        legendsize: '100',
        xaxislabel: null,
        yaxislabel: null,
        title: null,
        LegendTitle: "Legend",
        pieborderColor: "#fff",
        pieborderWidth: 2
    };

    function Plugin(element, options) {
        this.element = element;
        this.options = $.extend({}, defaults, options);
        this.init();
    }

    Plugin.prototype = {
        init: function () {

            var that = this,
           config = that.options;
            var graph = $(that.element).addClass("SimpleChart").addClass(config.ChartType).append("<canvas class='SimpleChartcanvas'></canvas>").find('canvas').css({
                float: (config.legendposition == 'right' || config.legendposition == 'left') ? 'left' : '',
                'margin-top': config.topmargin,
                'margin-right': config.rightmargin
            });
            var ctx = graph[0].getContext("2d");
            graph[0].width = $(that.element).width() - (config.showlegends ? ((config.legendposition == 'right' || config.legendposition == 'left') ? parseInt(config.legendsize) + parseInt(config.xPadding) : 0) : 0) - config.rightmargin;
            graph[0].height = $(that.element).height() - (config.showlegends ? ((config.legendposition == 'bottom' || config.legendposition == 'top') ? config.legendsize : 0) : 0) - config.topmargin;
            var c = graph[0].getContext('2d');
            switch (config.ChartType) {
                case "Line":
                    that.drawAxis(c, graph);
                    that.drawLineAreaScatteredHybridCharts(c, graph);
                    break;
                case "Area":
                    that.drawAxis(c, graph);
                    that.drawLineAreaScatteredHybridCharts(c, graph);
                    break;
                case "Scattered":
                    that.drawAxis(c, graph);
                    that.drawLineAreaScatteredHybridCharts(c, graph);
                    break;
                case "Hybrid":
                    that.drawAxis(c, graph);
                    that.drawLineAreaScatteredHybridCharts(c, graph);
                    that.drawBar(c, graph);
                    that.drawHybrid(c, graph);
                    break;
                case "Bar":
                    that.drawAxis(c, graph);
                    that.drawBar(c, graph);
                    break;
                case "Pie":
                    that.drawPie(c, graph);
                    break;
                case "Stacked":
                    that.drawAxis(c, graph);
                    that.drawStacked(c, graph);
                    break;
                case "StackedHybrid":
                    that.drawAxis(c, graph);
                    that.drawStacked(c, graph);
                    that.drawLineAreaScatteredHybridCharts(c, graph);
                    break;
            }

            //show legend
            if (config.showlegends) {
                that.drawLegends(graph);
            }
        },
        reload: function () {
            $(this.element).empty();
            this.init();
        },
        destroy: function () {
            $(this.element).empty();
        },
        FindYMax: function () {
            config = this.options;
            var max = 0;
            for (var i = 0; i < config.data.length; i++) {
                for (var j = 0; j < config.data[i].values.length; j++) {
                    if (config.data[i].values[j].Y > max) {
                        max = config.data[i].values[j].Y;
                    }
                }
            }
            max += 10 - max % 10;
            return max;
        },
        pixelX: function (val, i) {
            config = this.options;
            var graph = $(this.element).find('.SimpleChartcanvas');
            return ((graph.width() - config.xPadding) / config.data[i].values.length) * val + (config.xPadding * 1.5);
        },
        pixelY: function (val) {
            config = this.options;
            var graph = $(this.element).find('.SimpleChartcanvas');
            return graph.height() - (((graph.height() - config.yPadding) / this.FindYMax()) * val) - config.yPadding;
        },
        getRandomColor: function () {
            var letters = '0123456789ABCDEF'.split('');
            var color = '#';
            for (var i = 0; i < 6; i++) {
                color += letters[Math.floor(Math.random() * 16)];
            }
            return color;
        },
        drawAxis: function (c, graph) {
            var that = this, xelementarray = new Array(),
            config = this.options;
            c.lineWidth = 2;
            c.strokeStyle = config.axiscolor;
            c.font = config.font;
            c.textAlign = config.textAlign;


            c.beginPath();
            c.moveTo(config.xPadding, 0);
            c.lineTo(config.xPadding, graph.height() - config.yPadding);
            c.lineTo(graph.width(), graph.height() - config.yPadding);
            c.stroke();

            c.fillStyle = config.textcolor;

            for (var i = 0; i < config.data.length; i++) {
                for (var j = 0; j < config.data[i].values.length; j++) {
                    if (xelementarray.indexOf(config.data[i].values[j].X) < 0) {
                        xelementarray.push(config.data[i].values[j].X);
                        c.fillText(config.data[i].values[j].X, that.pixelX(j, i), graph.height() - config.yPadding + 20);
                    }
                }
            }
            c.save();
            var fontArgs = c.font.split(' ');
            c.font = config.axisfontsize + ' ' + fontArgs[fontArgs.length - 1];
            if (config.xaxislabel) {
                c.fillText(config.xaxislabel, graph.width() / 2, graph.height());
            }
            if (config.yaxislabel) {
                c.save();
                c.translate(0, graph.height() / 2);
                c.rotate(-Math.PI / 2);
                c.fillText(config.yaxislabel, 0, 15);
                c.restore();
            }
            if (config.title) {
                $("<div class='simple-chart-Header' />").appendTo($(that.element)).html(config.title).css({
                    left: graph.width() / 2 - ($(that.element).find('.simple-chart-Header').width() / 2),
                    top: 5
                });
            }
            c.restore();

            c.textAlign = "right"
            c.textBaseline = "middle";
            var maxY = that.FindYMax();
            var incrementvalue = "";
            for (var i = 0 ; i < Math.ceil(maxY).toString().length - 1; i++) {
                incrementvalue += "0";
            }
            incrementvalue = "1" + incrementvalue;
            incrementvalue = Math.ceil(maxY / parseInt(incrementvalue)) * Math.pow(10, (Math.ceil(maxY / 10).toString().length - 1));
            for (var i = 0; i < that.FindYMax() ; i += parseInt(incrementvalue)) {
                c.fillStyle = config.textcolor;
                c.fillText(i, config.xPadding - 10, that.pixelY(i));
                c.fillStyle = config.axiscolor;
                c.beginPath();
                c.arc(config.xPadding, that.pixelY(i), 6, 0, Math.PI * 2, true);
                c.fill();
            }
        },
        drawPie: function (c, graph) {
            var that = this,
           config = this.options;
            c.clearRect(0, 0, graph.width(), graph.height());
            var totalVal = 0, lastend = 0;
            for (var j = 0; j < config.data[0].values.length; j++) {
                totalVal += (typeof config.data[0].values[j].Y == 'number') ? config.data[0].values[j].Y : 0;
            }

            for (var i = 0; i < config.data[0].values.length; i++) {
                c.fillStyle = config.data[0].linecolor == "Random" ? config.data[0].values[i].color = randomcolor = that.getRandomColor() : config.data[0].linecolor;
                c.beginPath();
                var centerx = graph.width() / 2.2;
                var centery = graph.height() / 2.2;
                c.moveTo(centerx, centery);
                c.arc(centerx, centery, (config.legendposition == 'right' || config.legendposition == 'left') ? centerx : centery, lastend, lastend +
                  (Math.PI * 2 * (config.data[0].values[i].Y / totalVal)), false);
                c.lineTo(centerx, centery);
                c.fill();
                c.fillStyle = config.pielabelcolor;
                c.lineWidth = config.pieborderWidth;
                c.strokeStyle = config.pieborderColor;
                c.stroke();

                if (config.showpielables) {
                    c.save();
                    c.translate(centerx, centery);
                    c.rotate(lastend - 0.20 +
                      (Math.PI * 2 * (config.data[0].values[i].Y / totalVal)));
                    var dx = Math.floor(centerx * 0.5) + 40;
                    var dy = Math.floor(centery * 0.05);
                    c.textAlign = "right";
                    var fontArgs = c.font.split(' ');
                    c.font = config.piefontsize + ' ' + fontArgs[fontArgs.length - 1];
                    c.fillText(config.data[0].values[i].X, dx, dy);
                    c.restore();

                    c.save();
                    c.fillStyle = config.pielabelpercentcolor;
                    c.translate(centerx, centery);
                    c.rotate(lastend - 0.15 +
                      (Math.PI * 2 * (config.data[0].values[i].Y / totalVal)));
                    var dx = Math.floor(centerx * 0.5) + 90;
                    var dy = Math.floor(centery * 0.05);
                    c.textAlign = "right";
                    var fontArgs = c.font.split(' ');
                    c.font = config.piefontsize + ' ' + fontArgs[fontArgs.length - 1];
                    c.fillText(Math.round((config.data[0].values[i].Y / totalVal) * 100) + "%", dx, dy);
                    c.restore();
                }
                lastend += Math.PI * 2 * (config.data[0].values[i].Y / totalVal);
            }
            var canvasOffset = $(graph).offset();
            var offsetX = canvasOffset.left;
            var offsetY = canvasOffset.top;

        },
        drawBar: function (c, graph) {
            var that = this,
            config = this.options;
            for (var i = 0; i < config.data[0].values.length; i++) {
                var randomcolor;
                c.strokeStyle = config.data[0].linecolor == "Random" ? config.data[0].values[i].color = randomcolor = that.getRandomColor() : config.data[0].linecolor;
                c.fillStyle = config.data[0].linecolor == "Random" ? randomcolor : config.data[0].linecolor;
                c.beginPath();
                c.rect(that.pixelX(i, 0) - config.yPadding / 4, that.pixelY(config.data[0].values[i].Y), config.yPadding / 2, graph.height() - that.pixelY(config.data[0].values[i].Y) - config.xPadding + 8);
                c.closePath();
                c.stroke();
                c.fill();
                c.textAlign = "left";
                c.fillStyle = "#000";
                c.fillText(config.data[0].values[i].Y, that.pixelX(i, 0) - config.yPadding / 4, that.pixelY(config.data[0].values[i].Y) + 7, 200);
            }
        },

        drawStacked: function (c, graph) {
            var that = this,
            config = this.options;
            for (var i = 0; i < config.data.length; i++) {
                for (var j = 0; j < config.data[i].values.length; j++) {
                    var randomcolor;
                    c.strokeStyle = config.data[i].linecolor == "Random" ? config.data[i].values[j].color = randomcolor = that.getRandomColor() : config.data[i].linecolor;
                    c.fillStyle = config.data[i].linecolor == "Random" ? randomcolor : config.data[i].linecolor;
                    c.beginPath();
                    c.rect(that.pixelX(j, 0) - config.yPadding / 4, that.pixelY(config.data[i].values[j].Y), config.yPadding / 2, graph.height() - that.pixelY(config.data[i].values[j].Y) - config.xPadding + 8);
                    c.closePath();
                    c.stroke();
                    c.fill();
                    c.textAlign = "left";
                    c.fillStyle = "#000";
                    c.fillText(config.data[i].values[j].Y, that.pixelX(j, 0) - config.yPadding / 4, that.pixelY(config.data[i].values[j].Y) + 7, 200);
                }
            }
        },

        drawHybrid: function (c, graph) {
            var that = this,
            config = this.options;
            var randomcolor;
            c.strokeStyle = config.data[0].linecolor == "Random" ? randomcolor = that.getRandomColor() : config.data[0].linecolor;
            c.beginPath();
            c.moveTo(that.pixelX(0, 0), that.pixelY(config.data[0].values[0].Y));
            for (var j = 1; j < config.data[0].values.length; j++) {
                c.lineTo(that.pixelX(j, 0), that.pixelY(config.data[0].values[j].Y));
            }
            c.stroke();
            c.fillStyle = config.data[0].linecolor == "Random" ? randomcolor : config.data[0].linecolor;
            for (var j = 0; j < config.data[0].values.length; j++) {
                c.beginPath();
                c.arc(that.pixelX(j, 0), that.pixelY(config.data[0].values[j].Y), 4, 0, Math.PI * 2, true);
                c.fill();
            }
        },
        drawLineAreaScatteredHybridCharts: function (c, graph) {
            var that = this,
            config = this.options;
            var tipCanvas = $(that.element).append("<canvas id='tip'></canvas><div class='down-triangle'></div>").find("#tip").attr('width', config.toolwidth).attr('height', config.toolheight);
            var tipCtx = tipCanvas[0].getContext("2d");
            var highlighter = $(that.element).append("<canvas id='highlighter'></canvas>").find('#highlighter').attr('width', "18").attr('height', "18");
            var higlightctx = highlighter[0].getContext("2d");
            var tipbaloontip = $(that.element).find('.down-triangle');
            var canvasOffset = $(graph).offset();
            var offsetX = canvasOffset.left;
            var offsetY = canvasOffset.top;
            $(graph[0]).on("mousemove", function (e) {
                drawToolTiponHover(e);
            });

            for (var i = 0; i < config.data.length; i++) {
                c.strokeStyle = config.data[i].linecolor == "Random" ? config.data[i].Randomlinecolor = that.getRandomColor() : config.data[i].linecolor;
                c.beginPath();
                c.moveTo(that.pixelX(0, i), that.pixelY(config.data[i].values[0].Y));
                if (config.ChartType !== "Scattered" && config.ChartType !== "Hybrid") {
                    for (var j = 1; j < config.data[i].values.length; j++) {
                        c.lineTo(that.pixelX(j, i), that.pixelY(config.data[i].values[j].Y));
                    }
                    c.stroke();
                }
                c.fillStyle = config.data[i].linecolor == "Random" ? config.data[i].Randomlinecolor : config.data[i].linecolor;
                if (config.ChartType == "Area") {
                    c.lineTo(that.pixelX(config.data[i].values.length - 1, i), that.pixelY(0));
                    c.lineTo(that.pixelX(0, 0), that.pixelY(0));
                    c.stroke();
                    c.fill();
                }
                if (config.ChartType == "Line" || config.ChartType == "Scattered" || config.ChartType == "StackedHybrid") {
                    for (var j = 0; j < config.data[i].values.length; j++) {
                        c.beginPath();
                        c.arc(that.pixelX(j, i), that.pixelY(config.data[i].values[j].Y), 4, 0, Math.PI * 2, true);
                        c.fill();
                    }
                }
            }

            var linepoints = [];
            for (var i = 0; i < config.data.length; i++) {
                for (var j = 0; j < config.data[i].values.length; j++) {
                    linepoints.push({
                        x: that.pixelX(j, i),
                        y: that.pixelY(config.data[i].values[j].Y),
                        r: 4,
                        rXr: 16,
                        tip: config.data[i].values[j].Y,
                        color: config.data[i].linecolor == "Random" ? config.data[i].Randomlinecolor : config.data[i].linecolor
                    });
                }
            }

            function drawToolTiponHover(e) {
                mouseX = parseInt(e.pageX - offsetX);
                mouseY = parseInt(e.pageY - offsetY);
                var hit = false;
                for (var i = 0; i < linepoints.length; i++) {
                    var dot = linepoints[i];
                    var dx = mouseX - dot.x;
                    var dy = mouseY - dot.y;
                    if (dx * dx + dy * dy < dot.rXr) {
                        tipCanvas[0].style.left = (dot.x - (tipCanvas[0].width / 2)) - 3 + "px";
                        tipCanvas[0].style.top = (dot.y - 21 - tipCanvas[0].height) + config.topmargin + "px";
                        tipCtx.clearRect(0, 0, tipCanvas[0].width, tipCanvas[0].height);
                        tipCtx.fillText(dot.tip, 5, 15);
                        tipbaloontip[0].style.left = (dot.x) - 7 + "px";
                        tipbaloontip[0].style.top = (dot.y + config.topmargin) - 19 + "px";
                        if (config.ChartType == "Line" || config.ChartType == "Scattered" || config.ChartType == "Hybrid" || config.ChartType == "StackedHybrid") {
                            highlighter[0].style.left = (dot.x) - 9 + "px";
                            highlighter[0].style.top = (dot.y + config.topmargin) - 9 + "px";
                        }
                        higlightctx.clearRect(0, 0, highlighter.width(), highlighter.height());
                        higlightctx.strokeStyle = dot.color;
                        higlightctx.beginPath();
                        higlightctx.arc(9, 9, 7, 0, 2 * Math.PI);
                        higlightctx.lineWidth = 2;
                        higlightctx.stroke();
                        hit = true;
                    }
                }
                if (!hit) {
                    tipCanvas[0].style.left = "-400px";
                    highlighter[0].style.left = "-400px";
                    tipbaloontip[0].style.left = "-400px";
                }
            }
        },
        drawLegends: function (graph) {
            var that = this,
            config = this.options;
            if (config.ChartType == "Line" || config.ChartType == "Area" || config.ChartType == "Scattered" || config.ChartType == "Stacked" || config.ChartType == "StackedHybrid") {
                var _legends = $("<div class='simple-chart-legends' />", { id: "legendsdiv" }).css({
                    width: (config.legendposition == 'right' || config.legendposition == 'left') ? (config.legendsize - 5) : graph.width(),
                    height: (config.legendposition == 'top' || config.legendposition == 'bottom') ? (config.legendsize - 5) : graph.height(),
                    float: (config.legendposition == 'right' || config.legendposition == 'left') ? 'left' : ''
                }).appendTo($(that.element));
                var _ul = $(_legends).append("<span>" + config.LegendTitle + "</span>").append("<ul />").find("ul")
                for (var i = 0; i < config.data.length; i++) {
                    $("<li />", { class: "legendsli" }).append("<span />").find('span').addClass("legendindicator").append('<span class="line" style="background: ' + (config.data[i].linecolor == "Random" ? config.data[i].Randomlinecolor : config.data[i].linecolor) + '"></span><span class="circle" style="background: ' + (config.data[i].linecolor == "Random" ? config.data[i].Randomlinecolor : config.data[i].linecolor) + '"></span>').parent().append("<span>" + config.data[i].title + "</span>").appendTo(_ul);
                }
                if (config.legendposition == 'top' || config.legendposition == 'left') {
                    $(_legends).insertBefore($(that.element).find('.SimpleChartcanvas'));
                }
                if (config.legendposition == 'right' || config.legendposition == 'left') {
                    $(_legends).addClass('vertical')
                }
                else {
                    $(_legends).addClass('horizontal');
                }
            }
            if (config.ChartType == "Bar" || config.ChartType == "Hybrid" || config.ChartType == "Pie") {
                var _legends = $("<div class='simple-chart-legends' />", { id: "legendsdiv" }).css({
                    width: (config.legendposition == 'right' || config.legendposition == 'left') ? (config.legendsize - 5) : graph.width(),
                    height: (config.legendposition == 'top' || config.legendposition == 'bottom') ? (config.legendsize - 5) : graph.height(),
                    float: (config.legendposition == 'right' || config.legendposition == 'left') ? 'left' : ''
                }).appendTo($(that.element));
                var _ul = $(_legends).append("<span>" + config.LegendTitle + "</span>").append("<ul />").find("ul")
                for (var i = 0; i < config.data[0].values.length; i++) {

                    $("<li />", { class: "legendsli" }).append("<span />").find('span').addClass("legendindicator").append('<span class="line" style="background: ' + (config.data[0].linecolor == "Random" ? config.data[0].values[i].color : config.data[0].linecolor) + '"></span><span class="circle" style="background: ' + (config.data[0].linecolor == "Random" ? config.data[0].values[i].color : config.data[0].linecolor) + '"></span>').parent().append("<span>" + config.data[0].values[i].X + "</span><span class='legendvalue'>" + (config.ChartType == 'Pie' ? config.data[0].values[i].Y : '') + "</span>").appendTo(_ul);
                }
                if (config.legendposition == 'top' || config.legendposition == 'left') {
                    $(_legends).insertBefore($(that.element).find('.SimpleChartcanvas'));
                }
                if (config.legendposition == 'right' || config.legendposition == 'left') {
                    $(_legends).addClass('vertical')
                }
                else {
                    $(_legends).addClass('horizontal');
                }
            }
        }
    }


    $.fn[pluginName] = function (options) {
        if (typeof options === "string") {
            var args = Array.prototype.slice.call(arguments, 1);
            this.each(function () {
                var plugin = $.data(this, 'plugin_' + pluginName);
                if (plugin[options]) {
                    plugin[options].apply(plugin, args);
                } else {
                    plugin['options'][options] = args[0];
                }
            });
        } else {
            return this.each(function () {
                if (!$.data(this, 'plugin_' + pluginName)) {
                    $.data(this, 'plugin_' + pluginName, new Plugin(this, options));
                }
            });
        }
    }
})(jQuery, window, document, undefined);
