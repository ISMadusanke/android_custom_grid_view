import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ismadusankea.customgridview.R;


public class CustomGridView extends View {
    private int numRows = 3;
    private int numColumns = 3;
    private int cellPadding = 10;
    private int cellMargin = 10;
    private int cellColor = Color.WHITE;
    private ShapeType shapeType = ShapeType.SQUARE;
    private Drawable backgroundDrawable;
    private Drawable[][] cellBackgroundDrawables;
    private Paint paint;
    private Path path;
    private float cellWidth;
    private float cellHeight;
    private OnCellClickListener onCellClickListener;

    public CustomGridView(Context context) {
        super(context);
        init(null, 0);
    }

    public CustomGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CustomGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomGridView, defStyle, 0);
        numRows = a.getInt(R.styleable.CustomGridView_numRows, 3);
        numColumns = a.getInt(R.styleable.CustomGridView_numColumns, 3);
        cellPadding = a.getDimensionPixelSize(R.styleable.CustomGridView_cellPadding, 10);
        cellMargin = a.getDimensionPixelSize(R.styleable.CustomGridView_cellMargin, 10);
        cellColor = a.getColor(R.styleable.CustomGridView_cellColor, Color.WHITE);
        int shapeTypeInt = a.getInt(R.styleable.CustomGridView_shapeType, 0);
        shapeType = ShapeType.values()[shapeTypeInt];
        backgroundDrawable = a.getDrawable(R.styleable.CustomGridView_backgroundDrawable);
        a.recycle();

        cellBackgroundDrawables = new Drawable[numRows][numColumns];
        paint = new Paint();
        paint.setColor(cellColor);
        paint.setAntiAlias(true);
        path = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int desiredWidth = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
        int desiredHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();

        width = resolveSize(desiredWidth, widthMeasureSpec);
        height = resolveSize(desiredHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();

        if (backgroundDrawable != null) {
            backgroundDrawable.setBounds(getPaddingLeft(), getPaddingTop(), width + getPaddingLeft(), height + getPaddingTop());
            backgroundDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.TRANSPARENT);
        }

        cellWidth = (float) (width - (numColumns - 1) * cellMargin - 2 * getPaddingLeft() - 2 * cellPadding) / numColumns;
        cellHeight = (float) (height - (numRows - 1)*cellMargin - 2 * getPaddingTop() - 2 * cellPadding) / numRows;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                float left = getPaddingLeft() + j * (cellWidth + cellMargin) + cellPadding;
                float top = getPaddingTop() + i * (cellHeight + cellMargin) + cellPadding;
                float right = left + cellWidth;
                float bottom = top + cellHeight;

                if (cellBackgroundDrawables[i][j] != null) {
                    cellBackgroundDrawables[i][j].setBounds((int) left, (int) top, (int) right, (int) bottom);
                    cellBackgroundDrawables[i][j].draw(canvas);
                } else {
                    drawCell(canvas, left, top, right, bottom);
                }
            }
        }
    }

    private void drawCell(Canvas canvas, float left, float top, float right, float bottom) {
        switch (shapeType) {
            case CIRCLE:
                canvas.drawCircle((left + right) / 2, (top + bottom) / 2, Math.min(cellWidth, cellHeight) / 2, paint);
                break;
            case TRIANGLE:
                path.reset();
                path.moveTo((left + right) / 2, top);
                path.lineTo(left, bottom);
                path.lineTo(right, bottom);
                path.close();
                canvas.drawPath(path, paint);
                break;
            case SQUARE:
            default:
                canvas.drawRect(left, top, right, bottom, paint);
                break;
        }
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
        invalidate();
        requestLayout();
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        invalidate();
        requestLayout();
    }

    public void setCellPadding(int cellPadding) {
        this.cellPadding = cellPadding;
        invalidate();
        requestLayout();
    }

    public void setCellMargin(int cellMargin) {
        this.cellMargin = cellMargin;
        invalidate();
        requestLayout();
    }

    public void setCellColor(int cellColor) {
        this.cellColor = cellColor;
        paint.setColor(cellColor);
        invalidate();
        requestLayout();
    }

    public void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
        invalidate();
        requestLayout();
    }

    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        this.backgroundDrawable = backgroundDrawable;
        invalidate();
        requestLayout();
    }

    public void setCellBackgroundDrawable(Drawable drawable, int row, int column) {
        cellBackgroundDrawables[row][column] = drawable;
        invalidate();
        requestLayout();
    }

    public void setOnCellClickListener(OnCellClickListener listener) {
        this.onCellClickListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            int column = (int) ((x - getPaddingLeft()) / (cellWidth + cellMargin));
            int row = (int) ((y - getPaddingTop()) / (cellHeight + cellMargin));

            if (row >= 0 && row < numRows && column >= 0 && column < numColumns) {
                if (onCellClickListener != null) {
                    onCellClickListener.onCellClick(row, column);
                }
                return true;
            }
        }

        return super.onTouchEvent(event);
    }

    public interface OnCellClickListener {
        void onCellClick(int row, int column);
    }

    public enum ShapeType {
        RECTANGLE, CIRCLE, SQUARE, TRIANGLE
    }
}
