package libs.opensimplex2;

public class OpenSimplex2 {
    private static float[] GRADIENTS_2D;
    private static float[] GRADIENTS_3D;
    private static float[] GRADIENTS_4D;

    public static float noise2(long seed, double x, double y) {
        double s = 0.366025403784439 * (x + y);
        double xs = x + s;
        double ys = y + s;
        return OpenSimplex2.noise2_UnskewedBase(seed, xs, ys);
    }

    public static float noise2_ImproveX(long seed, double x, double y) {
        double xx = x * 0.7071067811865476;
        double yy = y * 1.2247448713915896;
        return OpenSimplex2.noise2_UnskewedBase(seed, yy + xx, yy - xx);
    }

    private static float noise2_UnskewedBase(long seed, double xs, double ys) {
        float a1;
        int xsb = OpenSimplex2.fastFloor(xs);
        int ysb = OpenSimplex2.fastFloor(ys);
        float xi = (float)(xs - (double)xsb);
        float yi = (float)(ys - (double)ysb);
        long xsbp = (long)xsb * 5910200641878280303L;
        long ysbp = (long)ysb * 6452764530575939509L;
        float t = (xi + yi) * -0.21132487f;
        float dx0 = xi + t;
        float dy0 = yi + t;
        float value = 0.0f;
        float a0 = 0.5f - dx0 * dx0 - dy0 * dy0;
        float dx2 = 0;
        float dy2 = 0;
        if (a0 > 0.0f) {
            value = a0 * a0 * (a0 * a0) * OpenSimplex2.grad(seed, xsbp, ysbp, dx0, dy0);
        }
        if ((a1 = -3.1547005f * t + (-0.6666667f + a0)) > 0.0f) {
            float dx1 = dx0 - 0.57735026f;
            float dy1 = dy0 - 0.57735026f;
            value += a1 * a1 * (a1 * a1) * OpenSimplex2.grad(seed, xsbp + 5910200641878280303L, ysbp + 6452764530575939509L, dx1, dy1);
        }
        if (dy0 > dx0) {
            dx2 = dx0 - -0.21132487f;
            dy2 = dy0 - 0.7886751f;
            float a2 = 0.5f - dx2 * dx2 - dy2 * dy2;
            if (a2 > 0.0f) {
                value += a2 * a2 * (a2 * a2) * OpenSimplex2.grad(seed, xsbp, ysbp + 6452764530575939509L, dx2, dy2);
            }
        } else {
            dx2 = dx0 - 0.7886751f;
            dy2 = dy0 - -0.21132487f;
            float a2 = 0.5f - dx2 * dx2 - dy2 * dy2;
            if (a2 > 0.0f) {
                value += a2 * a2 * (a2 * a2) * OpenSimplex2.grad(seed, xsbp + 5910200641878280303L, ysbp, dx2, dy2);
            }
        }
        return value;
    }

    public static float noise3_ImproveXY(long seed, double x, double y, double z) {
        double xy = x + y;
        double s2 = xy * -0.21132486540518713;
        double zz = z * 0.577350269189626;
        double xr = x + s2 + zz;
        double yr = y + s2 + zz;
        double zr = xy * -0.577350269189626 + zz;
        return OpenSimplex2.noise3_UnrotatedBase(seed, xr, yr, zr);
    }

    public static float noise3_ImproveXZ(long seed, double x, double y, double z) {
        double xz = x + z;
        double s2 = xz * -0.21132486540518713;
        double yy = y * 0.577350269189626;
        double xr = x + s2 + yy;
        double zr = z + s2 + yy;
        double yr = xz * -0.577350269189626 + yy;
        return OpenSimplex2.noise3_UnrotatedBase(seed, xr, yr, zr);
    }

    public static float noise3_Fallback(long seed, double x, double y, double z) {
        double r = 0.6666666666666666 * (x + y + z);
        double xr = r - x;
        double yr = r - y;
        double zr = r - z;
        return OpenSimplex2.noise3_UnrotatedBase(seed, xr, yr, zr);
    }

    private static float noise3_UnrotatedBase(long seed, double xr, double yr, double zr) {
        int xrb = OpenSimplex2.fastRound(xr);
        int yrb = OpenSimplex2.fastRound(yr);
        int zrb = OpenSimplex2.fastRound(zr);
        float xri = (float)(xr - (double)xrb);
        float yri = (float)(yr - (double)yrb);
        float zri = (float)(zr - (double)zrb);
        int xNSign = (int)(-1.0f - xri) | 1;
        int yNSign = (int)(-1.0f - yri) | 1;
        int zNSign = (int)(-1.0f - zri) | 1;
        float ax0 = (float)xNSign * -xri;
        float ay0 = (float)yNSign * -yri;
        float az0 = (float)zNSign * -zri;
        long xrbp = (long)xrb * 5910200641878280303L;
        long yrbp = (long)yrb * 6452764530575939509L;
        long zrbp = (long)zrb * 6614699811220273867L;
        float value = 0.0f;
        float a = 0.6f - xri * xri - (yri * yri + zri * zri);
        int l = 0;
        float b = 0;
        while (true) {
            if (a > 0.0f) {
                value += a * a * (a * a) * OpenSimplex2.grad(seed, xrbp, yrbp, zrbp, xri, yri, zri);
            }
            if (ax0 >= ay0 && ax0 >= az0) {
                b = a + ax0 + ax0;
                if (b > 1.0f) {
                    value += (b -= 1.0f) * b * (b * b) * OpenSimplex2.grad(seed, xrbp - (long)xNSign * 5910200641878280303L, yrbp, zrbp, xri + (float)xNSign, yri, zri);
                }
            } else if (ay0 > ax0 && ay0 >= az0) {
                b = a + ay0 + ay0;
                if (b > 1.0f) {
                    value += (b -= 1.0f) * b * (b * b) * OpenSimplex2.grad(seed, xrbp, yrbp - (long)yNSign * 6452764530575939509L, zrbp, xri, yri + (float)yNSign, zri);
                }
            } else {
                b = a + az0 + az0;
                if (b > 1.0f) {
                    value += (b -= 1.0f) * b * (b * b) * OpenSimplex2.grad(seed, xrbp, yrbp, zrbp - (long)zNSign * 6614699811220273867L, xri, yri, zri + (float)zNSign);
                }
            }
            if (l == 1) break;
            ax0 = 0.5f - ax0;
            ay0 = 0.5f - ay0;
            az0 = 0.5f - az0;
            xri = (float)xNSign * ax0;
            yri = (float)yNSign * ay0;
            zri = (float)zNSign * az0;
            a += 0.75f - ax0 - (ay0 + az0);
            xrbp += (long)(xNSign >> 1) & 0x5205402B9270C86FL;
            yrbp += (long)(yNSign >> 1) & 0x598CD327003817B5L;
            zrbp += (long)(zNSign >> 1) & 0x5BCC226E9FA0BACBL;
            xNSign = -xNSign;
            yNSign = -yNSign;
            zNSign = -zNSign;
            seed ^= 0xAD2AB84D169129D7L;
            ++l;
        }
        return value;
    }

    public static float noise4_ImproveXYZ_ImproveXY(long seed, double x, double y, double z, double w) {
        double xy = x + y;
        double s2 = xy * -0.211324865405187;
        double zz = z * 0.2886751345948129;
        double ww = w * 0.2236067977499788;
        double xr = x + (zz + ww + s2);
        double yr = y + (zz + ww + s2);
        double zr = xy * -0.577350269189626 + (zz + ww);
        double wr = z * -0.866025403784439 + ww;
        return OpenSimplex2.noise4_UnskewedBase(seed, xr, yr, zr, wr);
    }

    public static float noise4_ImproveXYZ_ImproveXZ(long seed, double x, double y, double z, double w) {
        double xz = x + z;
        double s2 = xz * -0.211324865405187;
        double yy = y * 0.2886751345948129;
        double ww = w * 0.2236067977499788;
        double xr = x + (yy + ww + s2);
        double zr = z + (yy + ww + s2);
        double yr = xz * -0.577350269189626 + (yy + ww);
        double wr = y * -0.866025403784439 + ww;
        return OpenSimplex2.noise4_UnskewedBase(seed, xr, yr, zr, wr);
    }

    public static float noise4_ImproveXYZ(long seed, double x, double y, double z, double w) {
        double xyz = x + y + z;
        double ww = w * 0.2236067977499788;
        double s2 = xyz * -0.16666666666666666 + ww;
        double xs = x + s2;
        double ys = y + s2;
        double zs = z + s2;
        double ws = -0.5 * xyz + ww;
        return OpenSimplex2.noise4_UnskewedBase(seed, xs, ys, zs, ws);
    }

    public static float noise4_ImproveXY_ImproveZW(long seed, double x, double y, double z, double w) {
        double s2 = (x + y) * -0.17827565795139938 + (z + w) * 0.21562339328884284;
        double t2 = (z + w) * -0.4039497625802071 + (x + y) * -0.3751990830100753;
        double xs = x + s2;
        double ys = y + s2;
        double zs = z + t2;
        double ws = w + t2;
        return OpenSimplex2.noise4_UnskewedBase(seed, xs, ys, zs, ws);
    }

    public static float noise4_Fallback(long seed, double x, double y, double z, double w) {
        double s = (double)-0.1381966f * (x + y + z + w);
        double xs = x + s;
        double ys = y + s;
        double zs = z + s;
        double ws = w + s;
        return OpenSimplex2.noise4_UnskewedBase(seed, xs, ys, zs, ws);
    }

    private static float noise4_UnskewedBase(long seed, double xs, double ys, double zs, double ws) {
        int xsb = OpenSimplex2.fastFloor(xs);
        int ysb = OpenSimplex2.fastFloor(ys);
        int zsb = OpenSimplex2.fastFloor(zs);
        int wsb = OpenSimplex2.fastFloor(ws);
        float xsi = (float)(xs - (double)xsb);
        float ysi = (float)(ys - (double)ysb);
        float zsi = (float)(zs - (double)zsb);
        float wsi = (float)(ws - (double)wsb);
        float siSum = xsi + ysi + (zsi + wsi);
        int startingLattice = (int)((double)siSum * 1.25);
        seed += (long)startingLattice * 1045921697555224141L;
        float startingLatticeOffset = (float)startingLattice * -0.2f;
        xsi += startingLatticeOffset;
        ysi += startingLatticeOffset;
        zsi += startingLatticeOffset;
        wsi += startingLatticeOffset;
        float ssi = (siSum + startingLatticeOffset * 4.0f) * 0.309017f;
        long xsvp = (long)xsb * 5910200641878280303L;
        long ysvp = (long)ysb * 6452764530575939509L;
        long zsvp = (long)zsb * 6614699811220273867L;
        long wsvp = (long)wsb * 6254464313819354443L;
        float value = 0.0f;
        int i = 0;
        while (true) {
            double score0 = 1.0 + (double)ssi * -3.2360678915486614;
            if (xsi >= ysi && xsi >= zsi && xsi >= wsi && (double)xsi >= score0) {
                xsvp += 5910200641878280303L;
                xsi -= 1.0f;
                ssi -= 0.309017f;
            } else if (ysi > xsi && ysi >= zsi && ysi >= wsi && (double)ysi >= score0) {
                ysvp += 6452764530575939509L;
                ysi -= 1.0f;
                ssi -= 0.309017f;
            } else if (zsi > xsi && zsi > ysi && zsi >= wsi && (double)zsi >= score0) {
                zsvp += 6614699811220273867L;
                zsi -= 1.0f;
                ssi -= 0.309017f;
            } else if (wsi > xsi && wsi > ysi && wsi > zsi && (double)wsi >= score0) {
                wsvp += 6254464313819354443L;
                wsi -= 1.0f;
                ssi -= 0.309017f;
            }
            float dx = xsi + ssi;
            float dy = ysi + ssi;
            float dz = zsi + ssi;
            float dw = wsi + ssi;
            float a = dx * dx + dy * dy + (dz * dz + dw * dw);
            if (a < 0.6f) {
                a -= 0.6f;
                a *= a;
                value += a * a * OpenSimplex2.grad(seed, xsvp, ysvp, zsvp, wsvp, dx, dy, dz, dw);
            }
            if (i == 4) break;
            xsi += 0.2f;
            ysi += 0.2f;
            zsi += 0.2f;
            wsi += 0.2f;
            ssi += 0.2472136f;
            seed -= 1045921697555224141L;
            if (i == startingLattice) {
                xsvp -= 5910200641878280303L;
                ysvp -= 6452764530575939509L;
                zsvp -= 6614699811220273867L;
                wsvp -= 6254464313819354443L;
                seed += 5229608487776120705L;
            }
            ++i;
        }
        return value;
    }

    private static float grad(long seed, long xsvp, long ysvp, float dx, float dy) {
        long hash = seed ^ xsvp ^ ysvp;
        hash *= 6026932503003350773L;
        hash ^= hash >> 58;
        int gi = (int)hash & 0xFE;
        return GRADIENTS_2D[gi | 0] * dx + GRADIENTS_2D[gi | 1] * dy;
    }

    private static float grad(long seed, long xrvp, long yrvp, long zrvp, float dx, float dy, float dz) {
        long hash = seed ^ xrvp ^ (yrvp ^ zrvp);
        hash *= 6026932503003350773L;
        hash ^= hash >> 58;
        int gi = (int)hash & 0x3FC;
        return GRADIENTS_3D[gi | 0] * dx + GRADIENTS_3D[gi | 1] * dy + GRADIENTS_3D[gi | 2] * dz;
    }

    private static float grad(long seed, long xsvp, long ysvp, long zsvp, long wsvp, float dx, float dy, float dz, float dw) {
        long hash = seed ^ (xsvp ^ ysvp) ^ (zsvp ^ wsvp);
        hash *= 6026932503003350773L;
        hash ^= hash >> 57;
        int gi = (int)hash & 0x7FC;
        return GRADIENTS_4D[gi | 0] * dx + GRADIENTS_4D[gi | 1] * dy + (GRADIENTS_4D[gi | 2] * dz + GRADIENTS_4D[gi | 3] * dw);
    }

    private static int fastFloor(double x) {
        int xi = (int)x;
        return x < (double)xi ? xi - 1 : xi;
    }

    private static int fastRound(double x) {
        return x < 0.0 ? (int)(x - 0.5) : (int)(x + 0.5);
    }

    static {
        int i;
        int i2;
        int i3;
        GRADIENTS_2D = new float[256];
        float[] grad2 = new float[]{0.38268343f, 0.9238795f, 0.9238795f, 0.38268343f, 0.9238795f, -0.38268343f, 0.38268343f, -0.9238795f, -0.38268343f, -0.9238795f, -0.9238795f, -0.38268343f, -0.9238795f, 0.38268343f, -0.38268343f, 0.9238795f, 0.13052619f, 0.9914449f, 0.6087614f, 0.7933533f, 0.7933533f, 0.6087614f, 0.9914449f, 0.13052619f, 0.9914449f, -0.13052619f, 0.7933533f, -0.6087614f, 0.6087614f, -0.7933533f, 0.13052619f, -0.9914449f, -0.13052619f, -0.9914449f, -0.6087614f, -0.7933533f, -0.7933533f, -0.6087614f, -0.9914449f, -0.13052619f, -0.9914449f, 0.13052619f, -0.7933533f, 0.6087614f, -0.6087614f, 0.7933533f, -0.13052619f, 0.9914449f};
        for (i3 = 0; i3 < grad2.length; ++i3) {
            grad2[i3] = (float)((double)grad2[i3] / 0.01001634121365712);
        }
        i3 = 0;
        int j = 0;
        while (i3 < GRADIENTS_2D.length) {
            if (j == grad2.length) {
                j = 0;
            }
            OpenSimplex2.GRADIENTS_2D[i3] = grad2[j];
            ++i3;
            ++j;
        }
        GRADIENTS_3D = new float[1024];
        float[] grad3 = new float[]{2.2247448f, 2.2247448f, -1.0f, 0.0f, 2.2247448f, 2.2247448f, 1.0f, 0.0f, 3.0862665f, 1.1721513f, 0.0f, 0.0f, 1.1721513f, 3.0862665f, 0.0f, 0.0f, -2.2247448f, 2.2247448f, -1.0f, 0.0f, -2.2247448f, 2.2247448f, 1.0f, 0.0f, -1.1721513f, 3.0862665f, 0.0f, 0.0f, -3.0862665f, 1.1721513f, 0.0f, 0.0f, -1.0f, -2.2247448f, -2.2247448f, 0.0f, 1.0f, -2.2247448f, -2.2247448f, 0.0f, 0.0f, -3.0862665f, -1.1721513f, 0.0f, 0.0f, -1.1721513f, -3.0862665f, 0.0f, -1.0f, -2.2247448f, 2.2247448f, 0.0f, 1.0f, -2.2247448f, 2.2247448f, 0.0f, 0.0f, -1.1721513f, 3.0862665f, 0.0f, 0.0f, -3.0862665f, 1.1721513f, 0.0f, -2.2247448f, -2.2247448f, -1.0f, 0.0f, -2.2247448f, -2.2247448f, 1.0f, 0.0f, -3.0862665f, -1.1721513f, 0.0f, 0.0f, -1.1721513f, -3.0862665f, 0.0f, 0.0f, -2.2247448f, -1.0f, -2.2247448f, 0.0f, -2.2247448f, 1.0f, -2.2247448f, 0.0f, -1.1721513f, 0.0f, -3.0862665f, 0.0f, -3.0862665f, 0.0f, -1.1721513f, 0.0f, -2.2247448f, -1.0f, 2.2247448f, 0.0f, -2.2247448f, 1.0f, 2.2247448f, 0.0f, -3.0862665f, 0.0f, 1.1721513f, 0.0f, -1.1721513f, 0.0f, 3.0862665f, 0.0f, -1.0f, 2.2247448f, -2.2247448f, 0.0f, 1.0f, 2.2247448f, -2.2247448f, 0.0f, 0.0f, 1.1721513f, -3.0862665f, 0.0f, 0.0f, 3.0862665f, -1.1721513f, 0.0f, -1.0f, 2.2247448f, 2.2247448f, 0.0f, 1.0f, 2.2247448f, 2.2247448f, 0.0f, 0.0f, 3.0862665f, 1.1721513f, 0.0f, 0.0f, 1.1721513f, 3.0862665f, 0.0f, 2.2247448f, -2.2247448f, -1.0f, 0.0f, 2.2247448f, -2.2247448f, 1.0f, 0.0f, 1.1721513f, -3.0862665f, 0.0f, 0.0f, 3.0862665f, -1.1721513f, 0.0f, 0.0f, 2.2247448f, -1.0f, -2.2247448f, 0.0f, 2.2247448f, 1.0f, -2.2247448f, 0.0f, 3.0862665f, 0.0f, -1.1721513f, 0.0f, 1.1721513f, 0.0f, -3.0862665f, 0.0f, 2.2247448f, -1.0f, 2.2247448f, 0.0f, 2.2247448f, 1.0f, 2.2247448f, 0.0f, 1.1721513f, 0.0f, 3.0862665f, 0.0f, 3.0862665f, 0.0f, 1.1721513f, 0.0f};
        for (i2 = 0; i2 < grad3.length; ++i2) {
            grad3[i2] = (float)((double)grad3[i2] / 0.07969837668935331);
        }
        i2 = 0;
        int j2 = 0;
        while (i2 < GRADIENTS_3D.length) {
            if (j2 == grad3.length) {
                j2 = 0;
            }
            OpenSimplex2.GRADIENTS_3D[i2] = grad3[j2];
            ++i2;
            ++j2;
        }
        GRADIENTS_4D = new float[2048];
        float[] grad4 = new float[]{-0.6740059f, -0.32398477f, -0.32398477f, 0.5794685f, -0.7504884f, -0.40046722f, 0.15296486f, 0.502986f, -0.7504884f, 0.15296486f, -0.40046722f, 0.502986f, -0.8828162f, 0.08164729f, 0.08164729f, 0.4553054f, -0.4553054f, -0.08164729f, -0.08164729f, 0.8828162f, -0.502986f, -0.15296486f, 0.40046722f, 0.7504884f, -0.502986f, 0.40046722f, -0.15296486f, 0.7504884f, -0.5794685f, 0.32398477f, 0.32398477f, 0.6740059f, -0.6740059f, -0.32398477f, 0.5794685f, -0.32398477f, -0.7504884f, -0.40046722f, 0.502986f, 0.15296486f, -0.7504884f, 0.15296486f, 0.502986f, -0.40046722f, -0.8828162f, 0.08164729f, 0.4553054f, 0.08164729f, -0.4553054f, -0.08164729f, 0.8828162f, -0.08164729f, -0.502986f, -0.15296486f, 0.7504884f, 0.40046722f, -0.502986f, 0.40046722f, 0.7504884f, -0.15296486f, -0.5794685f, 0.32398477f, 0.6740059f, 0.32398477f, -0.6740059f, 0.5794685f, -0.32398477f, -0.32398477f, -0.7504884f, 0.502986f, -0.40046722f, 0.15296486f, -0.7504884f, 0.502986f, 0.15296486f, -0.40046722f, -0.8828162f, 0.4553054f, 0.08164729f, 0.08164729f, -0.4553054f, 0.8828162f, -0.08164729f, -0.08164729f, -0.502986f, 0.7504884f, -0.15296486f, 0.40046722f, -0.502986f, 0.7504884f, 0.40046722f, -0.15296486f, -0.5794685f, 0.6740059f, 0.32398477f, 0.32398477f, 0.5794685f, -0.6740059f, -0.32398477f, -0.32398477f, 0.502986f, -0.7504884f, -0.40046722f, 0.15296486f, 0.502986f, -0.7504884f, 0.15296486f, -0.40046722f, 0.4553054f, -0.8828162f, 0.08164729f, 0.08164729f, 0.8828162f, -0.4553054f, -0.08164729f, -0.08164729f, 0.7504884f, -0.502986f, -0.15296486f, 0.40046722f, 0.7504884f, -0.502986f, 0.40046722f, -0.15296486f, 0.6740059f, -0.5794685f, 0.32398477f, 0.32398477f, -0.753341f, -0.3796829f, -0.3796829f, -0.3796829f, -0.78216845f, -0.43214726f, -0.43214726f, 0.121284805f, -0.78216845f, -0.43214726f, 0.121284805f, -0.43214726f, -0.78216845f, 0.121284805f, -0.43214726f, -0.43214726f, -0.85865086f, -0.5086297f, 0.04480237f, 0.04480237f, -0.85865086f, 0.04480237f, -0.5086297f, 0.04480237f, -0.85865086f, 0.04480237f, 0.04480237f, -0.5086297f, -0.9982829f, -0.033819415f, -0.033819415f, -0.033819415f, -0.3796829f, -0.753341f, -0.3796829f, -0.3796829f, -0.43214726f, -0.78216845f, -0.43214726f, 0.121284805f, -0.43214726f, -0.78216845f, 0.121284805f, -0.43214726f, 0.121284805f, -0.78216845f, -0.43214726f, -0.43214726f, -0.5086297f, -0.85865086f, 0.04480237f, 0.04480237f, 0.04480237f, -0.85865086f, -0.5086297f, 0.04480237f, 0.04480237f, -0.85865086f, 0.04480237f, -0.5086297f, -0.033819415f, -0.9982829f, -0.033819415f, -0.033819415f, -0.3796829f, -0.3796829f, -0.753341f, -0.3796829f, -0.43214726f, -0.43214726f, -0.78216845f, 0.121284805f, -0.43214726f, 0.121284805f, -0.78216845f, -0.43214726f, 0.121284805f, -0.43214726f, -0.78216845f, -0.43214726f, -0.5086297f, 0.04480237f, -0.85865086f, 0.04480237f, 0.04480237f, -0.5086297f, -0.85865086f, 0.04480237f, 0.04480237f, 0.04480237f, -0.85865086f, -0.5086297f, -0.033819415f, -0.033819415f, -0.9982829f, -0.033819415f, -0.3796829f, -0.3796829f, -0.3796829f, -0.753341f, -0.43214726f, -0.43214726f, 0.121284805f, -0.78216845f, -0.43214726f, 0.121284805f, -0.43214726f, -0.78216845f, 0.121284805f, -0.43214726f, -0.43214726f, -0.78216845f, -0.5086297f, 0.04480237f, 0.04480237f, -0.85865086f, 0.04480237f, -0.5086297f, 0.04480237f, -0.85865086f, 0.04480237f, 0.04480237f, -0.5086297f, -0.85865086f, -0.033819415f, -0.033819415f, -0.033819415f, -0.9982829f, -0.32398477f, -0.6740059f, -0.32398477f, 0.5794685f, -0.40046722f, -0.7504884f, 0.15296486f, 0.502986f, 0.15296486f, -0.7504884f, -0.40046722f, 0.502986f, 0.08164729f, -0.8828162f, 0.08164729f, 0.4553054f, -0.08164729f, -0.4553054f, -0.08164729f, 0.8828162f, -0.15296486f, -0.502986f, 0.40046722f, 0.7504884f, 0.40046722f, -0.502986f, -0.15296486f, 0.7504884f, 0.32398477f, -0.5794685f, 0.32398477f, 0.6740059f, -0.32398477f, -0.32398477f, -0.6740059f, 0.5794685f, -0.40046722f, 0.15296486f, -0.7504884f, 0.502986f, 0.15296486f, -0.40046722f, -0.7504884f, 0.502986f, 0.08164729f, 0.08164729f, -0.8828162f, 0.4553054f, -0.08164729f, -0.08164729f, -0.4553054f, 0.8828162f, -0.15296486f, 0.40046722f, -0.502986f, 0.7504884f, 0.40046722f, -0.15296486f, -0.502986f, 0.7504884f, 0.32398477f, 0.32398477f, -0.5794685f, 0.6740059f, -0.32398477f, -0.6740059f, 0.5794685f, -0.32398477f, -0.40046722f, -0.7504884f, 0.502986f, 0.15296486f, 0.15296486f, -0.7504884f, 0.502986f, -0.40046722f, 0.08164729f, -0.8828162f, 0.4553054f, 0.08164729f, -0.08164729f, -0.4553054f, 0.8828162f, -0.08164729f, -0.15296486f, -0.502986f, 0.7504884f, 0.40046722f, 0.40046722f, -0.502986f, 0.7504884f, -0.15296486f, 0.32398477f, -0.5794685f, 0.6740059f, 0.32398477f, -0.32398477f, -0.32398477f, 0.5794685f, -0.6740059f, -0.40046722f, 0.15296486f, 0.502986f, -0.7504884f, 0.15296486f, -0.40046722f, 0.502986f, -0.7504884f, 0.08164729f, 0.08164729f, 0.4553054f, -0.8828162f, -0.08164729f, -0.08164729f, 0.8828162f, -0.4553054f, -0.15296486f, 0.40046722f, 0.7504884f, -0.502986f, 0.40046722f, -0.15296486f, 0.7504884f, -0.502986f, 0.32398477f, 0.32398477f, 0.6740059f, -0.5794685f, -0.32398477f, 0.5794685f, -0.6740059f, -0.32398477f, -0.40046722f, 0.502986f, -0.7504884f, 0.15296486f, 0.15296486f, 0.502986f, -0.7504884f, -0.40046722f, 0.08164729f, 0.4553054f, -0.8828162f, 0.08164729f, -0.08164729f, 0.8828162f, -0.4553054f, -0.08164729f, -0.15296486f, 0.7504884f, -0.502986f, 0.40046722f, 0.40046722f, 0.7504884f, -0.502986f, -0.15296486f, 0.32398477f, 0.6740059f, -0.5794685f, 0.32398477f, -0.32398477f, 0.5794685f, -0.32398477f, -0.6740059f, -0.40046722f, 0.502986f, 0.15296486f, -0.7504884f, 0.15296486f, 0.502986f, -0.40046722f, -0.7504884f, 0.08164729f, 0.4553054f, 0.08164729f, -0.8828162f, -0.08164729f, 0.8828162f, -0.08164729f, -0.4553054f, -0.15296486f, 0.7504884f, 0.40046722f, -0.502986f, 0.40046722f, 0.7504884f, -0.15296486f, -0.502986f, 0.32398477f, 0.6740059f, 0.32398477f, -0.5794685f, 0.5794685f, -0.32398477f, -0.6740059f, -0.32398477f, 0.502986f, -0.40046722f, -0.7504884f, 0.15296486f, 0.502986f, 0.15296486f, -0.7504884f, -0.40046722f, 0.4553054f, 0.08164729f, -0.8828162f, 0.08164729f, 0.8828162f, -0.08164729f, -0.4553054f, -0.08164729f, 0.7504884f, -0.15296486f, -0.502986f, 0.40046722f, 0.7504884f, 0.40046722f, -0.502986f, -0.15296486f, 0.6740059f, 0.32398477f, -0.5794685f, 0.32398477f, 0.5794685f, -0.32398477f, -0.32398477f, -0.6740059f, 0.502986f, -0.40046722f, 0.15296486f, -0.7504884f, 0.502986f, 0.15296486f, -0.40046722f, -0.7504884f, 0.4553054f, 0.08164729f, 0.08164729f, -0.8828162f, 0.8828162f, -0.08164729f, -0.08164729f, -0.4553054f, 0.7504884f, -0.15296486f, 0.40046722f, -0.502986f, 0.7504884f, 0.40046722f, -0.15296486f, -0.502986f, 0.6740059f, 0.32398477f, 0.32398477f, -0.5794685f, 0.033819415f, 0.033819415f, 0.033819415f, 0.9982829f, -0.04480237f, -0.04480237f, 0.5086297f, 0.85865086f, -0.04480237f, 0.5086297f, -0.04480237f, 0.85865086f, -0.121284805f, 0.43214726f, 0.43214726f, 0.78216845f, 0.5086297f, -0.04480237f, -0.04480237f, 0.85865086f, 0.43214726f, -0.121284805f, 0.43214726f, 0.78216845f, 0.43214726f, 0.43214726f, -0.121284805f, 0.78216845f, 0.3796829f, 0.3796829f, 0.3796829f, 0.753341f, 0.033819415f, 0.033819415f, 0.9982829f, 0.033819415f, -0.04480237f, 0.04480237f, 0.85865086f, 0.5086297f, -0.04480237f, 0.5086297f, 0.85865086f, -0.04480237f, -0.121284805f, 0.43214726f, 0.78216845f, 0.43214726f, 0.5086297f, -0.04480237f, 0.85865086f, -0.04480237f, 0.43214726f, -0.121284805f, 0.78216845f, 0.43214726f, 0.43214726f, 0.43214726f, 0.78216845f, -0.121284805f, 0.3796829f, 0.3796829f, 0.753341f, 0.3796829f, 0.033819415f, 0.9982829f, 0.033819415f, 0.033819415f, -0.04480237f, 0.85865086f, -0.04480237f, 0.5086297f, -0.04480237f, 0.85865086f, 0.5086297f, -0.04480237f, -0.121284805f, 0.78216845f, 0.43214726f, 0.43214726f, 0.5086297f, 0.85865086f, -0.04480237f, -0.04480237f, 0.43214726f, 0.78216845f, -0.121284805f, 0.43214726f, 0.43214726f, 0.78216845f, 0.43214726f, -0.121284805f, 0.3796829f, 0.753341f, 0.3796829f, 0.3796829f, 0.9982829f, 0.033819415f, 0.033819415f, 0.033819415f, 0.85865086f, -0.04480237f, -0.04480237f, 0.5086297f, 0.85865086f, -0.04480237f, 0.5086297f, -0.04480237f, 0.78216845f, -0.121284805f, 0.43214726f, 0.43214726f, 0.85865086f, 0.5086297f, -0.04480237f, -0.04480237f, 0.78216845f, 0.43214726f, -0.121284805f, 0.43214726f, 0.78216845f, 0.43214726f, 0.43214726f, -0.121284805f, 0.753341f, 0.3796829f, 0.3796829f, 0.3796829f};
        for (i = 0; i < grad4.length; ++i) {
            grad4[i] = (float)((double)grad4[i] / 0.0220065933241897);
        }
        i = 0;
        int j3 = 0;
        while (i < GRADIENTS_4D.length) {
            if (j3 == grad4.length) {
                j3 = 0;
            }
            OpenSimplex2.GRADIENTS_4D[i] = grad4[j3];
            ++i;
            ++j3;
        }
    }
}