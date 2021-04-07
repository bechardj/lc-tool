
function slope(line) {
    let denominator = line[2] - line[0];
    return line[3] - line[1] / denominator !== 0 ? denominator : 0.001;
}

function pointWithinLineSegment(line, x, y) {
    let max_x = Math.max(line[0], line[2]);
    let min_x = Math.min(line[0], line[2]);
    // let max_y = Math.max(line[1], line[3]);
    // let min_y = Math.min(line[1], line[3]);

    return x > min_x && x < max_x;
}

function leftLineSegmentFromRectangle(rectangle) {
    let line = [];
    line.push(rectangle[0]);
    line.push(rectangle[1]);
    line.push(rectangle[0]);
    line.push(rectangle[1] + rectangle[3]);
    return line;
}

function rightLineSegmentFromRectangle(rectangle) {
    let line = [];
    line.push(rectangle[0] + rectangle[2]);
    line.push(rectangle[1]);
    line.push(rectangle[0] + rectangle[2]);
    line.push(rectangle[1] + rectangle[3]);
    return line;
}

function lineSegmentsInterceptsVertical(line1, line2) {
    let slope1 = slope(line1);
    let b1 = (-1 * slope1 * line2[0]) + line1[1];

    let y = slope1 * line2[0] + b1;

    return Math.min(line2[1], line2[3]) < y && Math.max(line2[1], line2[3]) > y
        && pointWithinLineSegment(line1, line2[0], y);

}
