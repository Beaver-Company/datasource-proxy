package net.ttddyy.dsproxy.test.hamcrest;

import net.ttddyy.dsproxy.test.ParameterByIndexHolder;
import net.ttddyy.dsproxy.test.ParameterByNameHolder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static net.ttddyy.dsproxy.test.hamcrest.ParameterHolderAssertions.paramIndexes;
import static net.ttddyy.dsproxy.test.hamcrest.ParameterHolderAssertions.paramNames;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * @author Tadaya Tsuyukubo
 * @since 1.4
 */
public class ParameterHolderAssertionsTest {

    @Test
    public void testParamNames() {
        ParameterByNameHolder holder = mock(ParameterByNameHolder.class);
        given(holder.getParamNames()).willReturn(Arrays.asList("foo", "bar"));

        Assert.assertThat(holder, paramNames(hasItem("foo")));
        Assert.assertThat(holder, paramNames("foo", "bar"));
    }

    @Test
    public void testParamNamesUnmatchedMessage() {
        ParameterByNameHolder holder = mock(ParameterByNameHolder.class);
        given(holder.getParamNames()).willReturn(Arrays.asList("foo", "bar"));

        try {
            Assert.assertThat(holder, paramNames(hasItem("BAZ")));
            fail("assertion should fail");
        } catch (AssertionError e) {
            assertThat(e).hasMessage("\nExpected: parameter names as a collection containing \"BAZ\"\n     but: mismatches were: [was \"foo\", was \"bar\"]");

        }
    }

    @Test
    public void testParamIndexes() {
        ParameterByIndexHolder holder = mock(ParameterByIndexHolder.class);
        given(holder.getParamIndexes()).willReturn(Arrays.asList(1, 2, 3));

        Assert.assertThat(holder, paramIndexes(hasItem(1)));
        Assert.assertThat(holder, paramIndexes(1, 3));
    }

    @Test
    public void testParamIndexesUnmatchedMessage() {
        ParameterByIndexHolder holder = mock(ParameterByIndexHolder.class);
        given(holder.getParamIndexes()).willReturn(Arrays.asList(1, 2));

        try {
            Assert.assertThat(holder, paramIndexes(hasItem(100)));
            fail("assertion should fail");
        } catch (AssertionError e) {
            assertThat(e).hasMessage("\nExpected: parameter indexes as a collection containing <100>\n     but: mismatches were: [was <1>, was <2>]");

        }
    }
}