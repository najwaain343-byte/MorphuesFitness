package com.example.morpheusfitness;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.List;

public class OverlayView extends View {

    // =========================================================
    // MEDIAPIPE RESULT
    // =========================================================

    private PoseLandmarkerResult results;


    // =========================================================
    // PAINT
    // =========================================================

    private final Paint lineOutlinePaint = new Paint();
    private final Paint linePaint = new Paint();

    private final Paint jointOuterPaint = new Paint();
    private final Paint jointInnerPaint = new Paint();


    // =========================================================
    // COLORS
    // =========================================================

    // Match Morpheus Fitness green theme
    private static final int SKELETON_GREEN =
            Color.rgb(46, 125, 50);

    private static final int JOINT_WHITE =
            Color.WHITE;

    private static final int OUTLINE_COLOR =
            Color.argb(170, 0, 0, 0);


    // =========================================================
    // SIZES
    // =========================================================

    private static final float LINE_OUTLINE_WIDTH = 10f;
    private static final float LINE_WIDTH = 6f;

    private static final float JOINT_OUTER_RADIUS = 8f;
    private static final float JOINT_INNER_RADIUS = 4.5f;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public OverlayView(
            Context context,
            @Nullable AttributeSet attrs) {

        super(context, attrs);

        setupPaints();
    }


    // =========================================================
    // SETUP PAINTS
    // =========================================================

    private void setupPaints() {

        // -----------------------------------------------------
        // DARK OUTLINE BEHIND SKELETON
        // -----------------------------------------------------

        lineOutlinePaint.setColor(OUTLINE_COLOR);
        lineOutlinePaint.setStyle(Paint.Style.STROKE);
        lineOutlinePaint.setStrokeWidth(LINE_OUTLINE_WIDTH);
        lineOutlinePaint.setStrokeCap(Paint.Cap.ROUND);
        lineOutlinePaint.setStrokeJoin(Paint.Join.ROUND);
        lineOutlinePaint.setAntiAlias(true);


        // -----------------------------------------------------
        // MAIN GREEN SKELETON LINE
        // -----------------------------------------------------

        linePaint.setColor(SKELETON_GREEN);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(LINE_WIDTH);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setAntiAlias(true);


        // -----------------------------------------------------
        // OUTER JOINT
        // -----------------------------------------------------

        jointOuterPaint.setColor(SKELETON_GREEN);
        jointOuterPaint.setStyle(Paint.Style.FILL);
        jointOuterPaint.setAntiAlias(true);


        // -----------------------------------------------------
        // INNER JOINT
        // -----------------------------------------------------

        jointInnerPaint.setColor(JOINT_WHITE);
        jointInnerPaint.setStyle(Paint.Style.FILL);
        jointInnerPaint.setAntiAlias(true);
    }


    // =========================================================
    // RECEIVE RESULT FROM MEDIAPIPE
    // =========================================================

    public void setResults(
            PoseLandmarkerResult results) {

        this.results = results;

        postInvalidate();
    }


    // =========================================================
    // CLEAR OVERLAY
    // =========================================================

    public void clear() {

        results = null;

        postInvalidate();
    }


    // =========================================================
    // DRAW
    // =========================================================

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);


        if (results == null ||
                results.landmarks().isEmpty()) {

            return;
        }


        List<NormalizedLandmark> landmarks =
                results.landmarks().get(0);


        if (landmarks.size() < 33) {

            return;
        }


        // =====================================================
        // DRAW BODY CONNECTIONS
        // =====================================================


        // -----------------------------------------------------
        // SHOULDERS
        // -----------------------------------------------------

        drawConnection(canvas, landmarks, 11, 12);


        // -----------------------------------------------------
        // LEFT ARM
        // -----------------------------------------------------

        drawConnection(canvas, landmarks, 11, 13);
        drawConnection(canvas, landmarks, 13, 15);


        // -----------------------------------------------------
        // RIGHT ARM
        // -----------------------------------------------------

        drawConnection(canvas, landmarks, 12, 14);
        drawConnection(canvas, landmarks, 14, 16);


        // -----------------------------------------------------
        // LEFT HAND
        // -----------------------------------------------------

        drawConnection(canvas, landmarks, 15, 17);
        drawConnection(canvas, landmarks, 15, 19);
        drawConnection(canvas, landmarks, 15, 21);


        // -----------------------------------------------------
        // RIGHT HAND
        // -----------------------------------------------------

        drawConnection(canvas, landmarks, 16, 18);
        drawConnection(canvas, landmarks, 16, 20);
        drawConnection(canvas, landmarks, 16, 22);


        // -----------------------------------------------------
        // TORSO
        // -----------------------------------------------------

        drawConnection(canvas, landmarks, 11, 23);
        drawConnection(canvas, landmarks, 12, 24);

        drawConnection(canvas, landmarks, 23, 24);


        // -----------------------------------------------------
        // LEFT LEG
        // -----------------------------------------------------

        drawConnection(canvas, landmarks, 23, 25);
        drawConnection(canvas, landmarks, 25, 27);


        // -----------------------------------------------------
        // RIGHT LEG
        // -----------------------------------------------------

        drawConnection(canvas, landmarks, 24, 26);
        drawConnection(canvas, landmarks, 26, 28);


        // -----------------------------------------------------
        // LEFT FOOT
        // -----------------------------------------------------

        drawConnection(canvas, landmarks, 27, 29);
        drawConnection(canvas, landmarks, 29, 31);
        drawConnection(canvas, landmarks, 27, 31);


        // -----------------------------------------------------
        // RIGHT FOOT
        // -----------------------------------------------------

        drawConnection(canvas, landmarks, 28, 30);
        drawConnection(canvas, landmarks, 30, 32);
        drawConnection(canvas, landmarks, 28, 32);


        // =====================================================
        // DRAW IMPORTANT BODY JOINTS
        // =====================================================

        /*
         * We intentionally do not draw all 33 facial landmarks.
         *
         * For exercise monitoring, body joints are more useful
         * and the overlay looks cleaner.
         */


        // Shoulders
        drawJoint(canvas, landmarks.get(11));
        drawJoint(canvas, landmarks.get(12));


        // Elbows
        drawJoint(canvas, landmarks.get(13));
        drawJoint(canvas, landmarks.get(14));


        // Wrists
        drawJoint(canvas, landmarks.get(15));
        drawJoint(canvas, landmarks.get(16));


        // Hips
        drawJoint(canvas, landmarks.get(23));
        drawJoint(canvas, landmarks.get(24));


        // Knees
        drawJoint(canvas, landmarks.get(25));
        drawJoint(canvas, landmarks.get(26));


        // Ankles
        drawJoint(canvas, landmarks.get(27));
        drawJoint(canvas, landmarks.get(28));


        // Feet
        drawJoint(canvas, landmarks.get(31));
        drawJoint(canvas, landmarks.get(32));
    }


    // =========================================================
    // DRAW CONNECTION
    // =========================================================

    private void drawConnection(
            Canvas canvas,
            List<NormalizedLandmark> landmarks,
            int startIndex,
            int endIndex) {

        NormalizedLandmark start =
                landmarks.get(startIndex);

        NormalizedLandmark end =
                landmarks.get(endIndex);


        float startX =
                getXCoordinate(start);

        float startY =
                getYCoordinate(start);

        float endX =
                getXCoordinate(end);

        float endY =
                getYCoordinate(end);


        // -----------------------------------------------------
        // DRAW DARK OUTLINE FIRST
        // -----------------------------------------------------

        canvas.drawLine(
                startX,
                startY,
                endX,
                endY,
                lineOutlinePaint
        );


        // -----------------------------------------------------
        // DRAW GREEN LINE ON TOP
        // -----------------------------------------------------

        canvas.drawLine(
                startX,
                startY,
                endX,
                endY,
                linePaint
        );
    }


    // =========================================================
    // DRAW JOINT
    // =========================================================

    private void drawJoint(
            Canvas canvas,
            NormalizedLandmark landmark) {

        float x =
                getXCoordinate(landmark);

        float y =
                getYCoordinate(landmark);


        // Green outer circle

        canvas.drawCircle(
                x,
                y,
                JOINT_OUTER_RADIUS,
                jointOuterPaint
        );


        // White centre

        canvas.drawCircle(
                x,
                y,
                JOINT_INNER_RADIUS,
                jointInnerPaint
        );
    }


    // =========================================================
    // X COORDINATE
    // =========================================================

    private float getXCoordinate(
            NormalizedLandmark landmark) {

        /*
         * Camera frame is already mirrored
         * in MainActivity.
         *
         * Therefore use MediaPipe X directly.
         */

        return landmark.x() * getWidth();
    }


    // =========================================================
    // Y COORDINATE
    // =========================================================

    private float getYCoordinate(
            NormalizedLandmark landmark) {

        return landmark.y() * getHeight();
    }
}