package us.jbec.lct.util.geometry;

import org.springframework.stereotype.Component;
import us.jbec.lct.models.geometry.LabeledRectangle;
import us.jbec.lct.models.geometry.LineSegment;
import us.jbec.lct.models.geometry.OffsetRectangle;
import us.jbec.lct.models.geometry.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GeometricCollectionUtils {

    public Point uppermostLeftPoint(Collection<LabeledRectangle> rectangles) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        for (OffsetRectangle rectangle : rectangles) {
            if (rectangle.getX1() < minX) {
                minX = rectangle.getX1();
            }
            if (rectangle.getY1() < minY) {
                minY = rectangle.getY1();
            }
        }
        return new Point(minX, minY);
    }

    public Point lowermostRightPoint(Collection<LabeledRectangle> rectangles) {
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (OffsetRectangle rectangle : rectangles) {
            if (rectangle.getX2() > maxX) {
                maxX = rectangle.getX2();
            }
            if (rectangle.getY2() > maxY) {
                maxY = rectangle.getY2();
            }
        }
        return new Point(maxX, maxY);
    }

    public List<Set<LabeledRectangle>> groupLabeledRectanglesByLineSegment(Collection<LabeledRectangle> rectangles, Collection<LineSegment> lineSegments){
        List<Set<LabeledRectangle>> unGroupedRectangles = new ArrayList<>();
        for (var lineSegment : lineSegments) {
            Set<LabeledRectangle> thisGroupsRectangles = rectangles.stream()
                    .filter(lineSegment::interceptsRectangle)
                    .collect(Collectors.toSet());
            unGroupedRectangles.add(thisGroupsRectangles);
        }
        return unGroupedRectangles;
    }

    public List<Set<LabeledRectangle>> combineLabeledRectanglesByLineSegment(List<Set<LabeledRectangle>> ungroupedRectangles) {

        var ungroupedRectanglesSets = ungroupedRectangles.stream()
                .filter(set -> set.size() != 0)
                .toList();

        List<Set<LabeledRectangle>> groupedRectangles = new ArrayList<>();

        for (var ungroupedRectanglesSet : ungroupedRectanglesSets) {
            boolean grouped = false;
            for (var labeledRectangle : ungroupedRectanglesSet) {
                var matchingSet = groupedRectangles.stream().filter(set -> set.contains(labeledRectangle)).findFirst();
                if (matchingSet.isPresent()) {
                    grouped = true;
                    matchingSet.get().addAll(ungroupedRectanglesSet);
                    break;
                }
            }
            if (!grouped) {
                groupedRectangles.add(ungroupedRectanglesSet);
            }
        }

        return groupedRectangles.stream().filter(set -> set.size() != 0).toList();

    }

}
