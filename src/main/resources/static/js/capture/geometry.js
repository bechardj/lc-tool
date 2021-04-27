
class Rectangle {

    constructor(r) {
        this.x1 = r[0];
        this.y1 = r[1];
        this.width = r[2];
        this.height = r[3];
        if (this.width < 0) {
            this.x1 = this.x1 + this.width;
            this.width *= -1;
        }
        if (this.height < 0) {
            this.y1 = this.y1 + this.height;
            this.height *= -1;
        }
    }

    geometricPolygonRepresentation() {
        return [[this.x1, this.y1],
            [this.x1,  this.y1 + this.height],
            [this.x1 + this.width, this.y1 + this.height],
            [this.x1 + this.width, this.y1],
            [this.x1, this.y1]];
    }

    get getY2() {
        return this.y1 + this.height;
    }

    get x2() {
        return this.x1 + this.width;
    }

    get asArray() {
        return Array.of(this.x1, this.y1, this.width, this.height);
    }

    containsPoint(point) {
        return geometric.pointInPolygon(point, this.geometricPolygonRepresentation());
    }

    draw(context, outlineColor, outlineWidth, translucency, translucencyColor) {
        context.strokeStyle = outlineColor;
        context.lineWidth = outlineWidth;
        context.beginPath();
        context.rect(this.x1, this.y1, this.width, this.height);
        context.stroke();
        context.beginPath();
        if (translucency) {
            context.fillStyle = translucencyColor;
            context.globalAlpha = 0.4;
            context.fillRect(this.x1, this.y1, this.width, this.height);
            context.globalAlpha = 1.0
            context.stroke();
        }
    }

    drawLabel(context, label, labelColor, labelOutlineWidth, labelOutlineColor, font, fontSize) {
        if (label !== undefined) {
            context.font = "bold " + fontSize + "px " + font;
            context.fillStyle = labelColor;
            context.strokeStyle = labelOutlineColor;
            context.lineWidth = labelOutlineWidth;
            context.textAlign = "center";
            context.fillText(label, this.x1 + this.width / 2, this.y1 + (this.height / 2) + fontSize / 4);
            context.strokeText(label, this.x1 + this.width / 2, this.y1 + (this.height / 2) + fontSize / 4);
        }
    }

    static convertFromArrayOfPoints(arrayOfPoints) {
        return arrayOfPoints.map(r => new Rectangle(r));
    }

    static convertFromArrayOfRectangles(arrayOfRectangles) {
        return arrayOfRectangles.map(r => r.asArray);
    }
}

class Line {
    constructor(l) {
        this.x1 = l[0];
        this.y1 = l[1];
        this.x2 = l[2];
        this.y2 = l[3];
    }

    get asArray() {
        return Array.of(this.x1, this.y1, this.x2, this.y2);
    }

    geometricLineRepresentation() {
        return [[this.x1, this.y1], [this.x2, this.y2]];
    }

    intersectsRectangle(rectangle) {
        return geometric.lineIntersectsPolygon(this.geometricLineRepresentation(), rectangle.geometricPolygonRepresentation());
    }

    pointNearLine(point, distance) {
        // See https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
        let numerator = Math.abs(((this.x2-this.x1) * (this.y1-point[1])) - ((this.x1-point[0])*(this.y2-this.y1)));
        let denominator = Math.sqrt(Math.pow((this.x2-this.x1),2) + Math.pow((this.y2-this.y1), 2));
        return numerator / denominator < distance &&
            (Math.min(this.x1, this.x2) <= point[0] && point[0] <= Math.max(this.x1, this.x2)) &&
            (Math.min(this.y1, this.y2) <= point[1] && point[1] <= Math.max(this.y1, this.y2));
    }

    length() {
        return geometric.lineLength(this.geometricLineRepresentation());
    }

    draw(context, color, width) {
        context.beginPath();
        context.moveTo(this.x1, this.y1);
        context.lineTo(this.x2, this.y2);
        context.strokeStyle = color;
        context.lineWidth = width;
        context.stroke();
    }

    static convertFromArrayOfPoints(arrayOfPoints) {
        return arrayOfPoints.map(l => new Line(l));
    }

    static convertFromArrayOfLines(arrayOfLines) {
        return arrayOfLines.map(l => l.asArray);
    }
}

export { Rectangle, Line };

