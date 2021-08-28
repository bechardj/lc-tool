class LabeledRectangle {
    x1;
    y1;
    height;
    width;
    label;
    constructor(points) {
        if (points !== undefined) {
            this.x1 = points[0];
            this.y1 = points[1];
            this.width = points[2];
            this.height = points[3];

            if (this.width < 0) {
                this.x1 = this.x1 + this.width;
                this.width *= -1;
            }

            if (this.height < 0) {
                this.y1 = this.y1 + this.height;
                this.height *= -1;
            }
        }
    }
}


class Rectangle {
    uuid;
    labeledRectangle;

    constructor(points) {
        this.labeledRectangle = new LabeledRectangle(points);
        this.captureDataRecordType = "CREATE";
        this.uuid = UUID.genV4().hexString;
    }

    initFromExisting(rect) {
        this.labeledRectangle = new LabeledRectangle(rect.asArray);
        this.captureDataRecordType = "CREATE";
        this.uuid = UUID.genV4().hexString;
        this.labeledRectangle.label = rect.labeledRectangle.label;
    }

    geometricPolygonRepresentation() {
        let rect = this.labeledRectangle;
        return [[rect.x1, rect.y1],
            [rect.x1,  rect.y1 + rect.height],
            [rect.x1 + rect.width, rect.y1 + rect.height],
            [rect.x1 + rect.width, rect.y1],
            [rect.x1, rect.y1]];
    }

    get getY2() {
        return this.labeledRectangle.y1 + this.labeledRectangle.height;
    }

    get x2() {
        return this.labeledRectangle.x1 + this.labeledRectangle.width;
    }

    get asArray() {
        return Array.of(this.labeledRectangle.x1, this.labeledRectangle.y1, this.labeledRectangle.width, this.labeledRectangle.height);
    }

    containsPoint(point) {
        return geometric.pointInPolygon(point, this.geometricPolygonRepresentation());
    }

    draw(context, outlineColor, outlineWidth, translucency, translucencyColor) {
        context.strokeStyle = outlineColor;
        context.lineWidth = outlineWidth;
        context.beginPath();
        const rect = this.labeledRectangle;
        context.rect(rect.x1, rect.y1, rect.width, rect.height);
        context.stroke();
        context.beginPath();
        if (translucency) {
            context.fillStyle = translucencyColor;
            context.globalAlpha = 0.4;
            context.fillRect(rect.x1, rect.y1, rect.width, rect.height);
            context.globalAlpha = 1.0
            context.stroke();
        }
    }

    drawLabel(context, labelColor, labelOutlineWidth, labelOutlineColor, font, fontSize) {
        const rect = this.labeledRectangle;
        if (rect.label !== undefined) {
            context.font = "bold " + fontSize + "px " + font;
            context.fillStyle = labelColor;
            context.strokeStyle = labelOutlineColor;
            context.lineWidth = labelOutlineWidth;
            context.textAlign = "center";
            context.fillText(rect.label, rect.x1 + rect.width / 2, rect.y1 + (rect.height / 2) + fontSize / 4);
            context.strokeText(rect.label, rect.x1 + rect.width / 2, rect.y1 + (rect.height / 2) + fontSize / 4);
        }
    }

    static convertFromArrayOfPoints(arrayOfPoints) {
        return arrayOfPoints.map(r => new Rectangle(r));
    }

    static convertFromArrayOfRectangles(arrayOfRectangles) {
        return arrayOfRectangles.map(r => r.asArray);
    }
}

class LineSegment {
    x1;
    y1;
    x2;
    y2;
    constructor(points) {
        if (points !== undefined) {
            this.x1 = points[0];
            this.y1 = points[1];
            this.x2 = points[2];
            this.y2 = points[3];
        }
    }
}

class Line {
    lineSegment;
    constructor(points) {
        this.captureDataRecordType = "CREATE";
        this.uuid = UUID.genV4().hexString;
        this.lineSegment = new LineSegment(points);
    }

    initFromExisting(line) {
        this.line = new LineSegment(line.asArray);
        this.captureDataRecordType = "CREATE";
        this.uuid = UUID.genV4().hexString;
    }

    get asArray() {
        return Array.of(this.lineSegment.x1, this.lineSegment.y1, this.lineSegment.x2, this.lineSegment.y2);
    }

    geometricLineRepresentation() {
        return [[this.lineSegment.x1, this.lineSegment.y1], [this.lineSegment.x2, this.lineSegment.y2]];
    }

    intersectsRectangle(rectangle) {
        return geometric.lineIntersectsPolygon(this.geometricLineRepresentation(), rectangle.geometricPolygonRepresentation());
    }

    pointNearLine(point, distance) {
        const x1 = this.lineSegment.x1;
        const y1 = this.lineSegment.y1;
        const x2 = this.lineSegment.x2;
        const y2 = this.lineSegment.y2;
        // See https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
        let numerator = Math.abs(((x2-x1) * (y1-point[1])) - ((x1-point[0])*(y2-y1)));
        let denominator = Math.sqrt(Math.pow((x2-x1),2) + Math.pow((y2-y1), 2));
        return numerator / denominator < distance &&
            (Math.min(x1, x2) <= point[0] && point[0] <= Math.max(x1, x2)) &&
            (Math.min(y1, y2) <= point[1] && point[1] <= Math.max(y1, y2));
    }

    length() {
        return geometric.lineLength(this.geometricLineRepresentation());
    }

    draw(context, color, width) {
        context.beginPath();
        context.moveTo(this.lineSegment.x1, this.lineSegment.y1);
        context.lineTo(this.lineSegment.x2, this.lineSegment.y2);
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

