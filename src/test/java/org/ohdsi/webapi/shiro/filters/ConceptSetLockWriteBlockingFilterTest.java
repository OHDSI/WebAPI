package org.ohdsi.webapi.shiro.filters;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.Mockito;
import org.ohdsi.webapi.service.lock.ConceptSetLockingService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;


public class ConceptSetLockWriteBlockingFilterTest {


	@Test
	public void shouldAllowAllIfSnapshotLockingIsDisabled() {
		ConceptSetLockWriteBlockingFilter sut = new ConceptSetLockWriteBlockingFilter(false, null);
		assertThat(sut.isPermitted("conceptset:1:put"), is(true));
	}

	@Test
	public void shouldRejectIfLockedConceptSet() {
		ConceptSetLockingService conceptSetLockingServiceMock = Mockito.mock(ConceptSetLockingService.class);
		when(conceptSetLockingServiceMock.areLocked(anyListOf(Integer.class))).thenReturn(ImmutableMap.of(1, true));
		ConceptSetLockWriteBlockingFilter sut = new ConceptSetLockWriteBlockingFilter(true, conceptSetLockingServiceMock);
		assertThat(sut.isPermitted("conceptset:1:put"), is(false));
		assertThat(sut.isPermitted("conceptset:1:annotation:put"), is(false));
		assertThat(sut.isPermitted("conceptset:1:items:put"), is(false));
	}

	@Test
	public void shouldAllowNonWriteConceptSetPaths() {
		ConceptSetLockingService conceptSetLockingServiceMock = Mockito.mock(ConceptSetLockingService.class);
		when(conceptSetLockingServiceMock.areLocked(anyListOf(Integer.class))).thenReturn(ImmutableMap.of(1, true));
		ConceptSetLockWriteBlockingFilter sut = new ConceptSetLockWriteBlockingFilter(true, conceptSetLockingServiceMock);
		assertThat(sut.isPermitted("conceptset:check:post"), is(true));
		assertThat(sut.isPermitted("conceptset:check-locked:post"), is(true));
		assertThat(sut.isPermitted("conceptset:1:get"), is(true));
		assertThat(sut.isPermitted("conceptset:1:annotation:get"), is(true));
		assertThat(sut.isPermitted("conceptset:1:items:get"), is(true));
	}


}
