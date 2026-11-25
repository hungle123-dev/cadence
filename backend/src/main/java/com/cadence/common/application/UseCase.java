package com.cadence.common.application;

/**
 * One write use case. Example: {@code interface PlaceOrderUseCase extends
 * UseCase<PlaceOrderCommand, OrderView> {}}. Read use cases are grouped per aggregate instead
 * ({@code ProductQueryUseCase}).
 */
@FunctionalInterface
public interface UseCase<I, O> {

    O execute(I input);
}
