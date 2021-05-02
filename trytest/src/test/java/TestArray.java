import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TestArray {
    private static Homework6 homework6;

    @BeforeAll
    public static void init() {
        homework6 = new Homework6();
    }

    public static Stream<Arguments> checkingDataException() {
        List<Arguments> data = new ArrayList<>();
        data.add(Arguments.arguments(new int[]{1, 2, 44, 24, 3, 7}));
        return data.stream();
    }

    @ParameterizedTest
    @MethodSource("checkingDataException")
    public void checkArrTestException(int[] actual) {
        Assertions.assertThrows(RuntimeException.class, () -> {
            homework6.checkArr(actual);
        });
    }

    public static Stream<Arguments> checkingData() {
        List<Arguments> data = new ArrayList<>();
        data.add(Arguments.arguments(new int[]{1, 7}, new int[]{1, 2, 4, 4, 2, 3, 4, 1, 7}));
        data.add(Arguments.arguments(new int[]{}, new int[]{1, 2, 4, 3, 4}));
        return data.stream();
    }

    @ParameterizedTest
    @MethodSource("checkingData")
    public void checkArrTest(int[] expected, int[] actual) {
        Assertions.assertArrayEquals(expected, homework6.checkArr(actual));
    }

    public static Stream<Arguments> isCheckContainData(){
        List<Arguments> data = new ArrayList<>();
        data.add(Arguments.arguments(true, new int[]{1, 1, 1, 4, 4, 1, 4, 4}));
        data.add(Arguments.arguments(false, new int[]{1, 1, 1, 1, 1, 1}));
        data.add(Arguments.arguments(false, new int[]{4, 4, 4, 4}));
        data.add(Arguments.arguments(false, new int[]{1, 2, 44, 24, 3, 7}));
        return data.stream();
    }

    @ParameterizedTest
    @MethodSource("isCheckContainData")
    public void isCheckContainTest(boolean result, int[] array) {
        Assertions.assertEquals(result, homework6.isCheckContain(array));
    }
}
