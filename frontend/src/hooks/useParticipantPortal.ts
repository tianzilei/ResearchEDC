import { participantPortalApi, type ParticipantPortalDTO, type ParticipantPortalTaskDTO } from "@/api/participantPortal";
import { useAppQuery } from "@/hooks/useQuery";

export type { ParticipantPortalDTO, ParticipantPortalTaskDTO };

export function useParticipantPortal(rawToken: string | undefined) {
  return useAppQuery<ParticipantPortalDTO>({
    queryKey: ["participant-portal", "bootstrap", rawToken],
    queryFn: () => participantPortalApi.bootstrap(rawToken ?? ""),
    enabled: !!rawToken,
    retry: false,
  });
}
