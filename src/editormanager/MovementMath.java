package editormanager;

public class MovementMath {
    public static class Vector2 {
        public float x = 0;
        public float y = 0;
        public float mag = 0;
        public Vector2(float _x, float _y) {
            x = _x;
            y = _y;

            adjustMag();
        }

        public Vector2() {}

        public void add(float _x, float _y) {
            x+=_x;
            y+=_y;
            adjustMag();
        }
        public void set(float _x, float _y) {
            x=_x;
            y=_y;
            adjustMag();
        }
        public void add(Vector2 vect) {
            x+=vect.x;
            y+=vect.y;
            adjustMag();
        }
        public void set(Vector2 vect) {
            x=vect.x;
            y=vect.y;
            adjustMag();
        }
        private void adjustMag() {
            mag = (float)Math.sqrt(x*x + y*y);
        }
    }

    //av is a number between
    public static float lerp(float num1, float num2, float av) {
        final float FILTER = .01f;
        if(num1<num2)
            if(num1>=num2-FILTER)
                return num2;
        else if(num1>num2)
            if(num1<=num2+FILTER)
                return num2;
        return (num1 * (1.0f - av) + (num2 * av));
    }
}
