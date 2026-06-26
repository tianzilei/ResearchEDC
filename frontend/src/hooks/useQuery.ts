import {
  useQuery,
  useMutation,
  useQueryClient,
  type UseQueryOptions,
  type UseMutationOptions,
} from "@tanstack/react-query";
import type { ApiError } from "@/api/client";

/**
 * Typed wrapper around TanStack Query's useQuery.
 *
 * Infers the return type TData and provides consistent error handling
 * across all queries in the application.
 */
export function useAppQuery<TData>(
  options: UseQueryOptions<TData, ApiError>,
) {
  return useQuery<TData, ApiError>(options);
}

/**
 * Typed wrapper around TanStack Query's useMutation.
 *
 * Infers TData (response) and TVariables (input) types.
 */
export function useAppMutation<TData, TVariables>(
  options: UseMutationOptions<TData, ApiError, TVariables>,
) {
  return useMutation<TData, ApiError, TVariables>(options);
}

export { useQueryClient };
