public class Homework6{

    public static int[] checkArr(int[] array) throws RuntimeException{
        int[] result;
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] == 4) {
                result = new int[array.length - (i + 1)];
                for (int j = 0; j < result.length; j++) {
                    result[j] = array[i + 1 + j];
                }
                return result;
            }
        }
        throw new RuntimeException("Not content 4");
    }

    public static boolean isCheckContain(int[] array) {
        boolean one = false;
        boolean fore = false;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != 1 && array[i] != 4) {
                return false;
            }
            if (array[i] == 4) {
                fore = true;
            }
            if (array[i] == 1) {
                one = true;
            }
        }
        if (one && fore) {
            return true;
        }
        else {
            return false;
        }
    }
}
