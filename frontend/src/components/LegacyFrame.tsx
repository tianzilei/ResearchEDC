import { useRef, useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import { Alert, Button, Card, Typography } from "antd";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Text } = Typography;

/**
 * Hybrid Shell: renders legacy JSP pages in an iframe inside the React AppShell.
 *
 * Route: `path: "legacy/*"` under `/app`. The `*` wildcard captures everything
 * after `/app/legacy/` and maps it to `/legacy/<path>` on the Spring Boot backend.
 *
 * @example /app/legacy/ViewStudySubjectList?studyId=1 → iframe src="/legacy/ViewStudySubjectList?studyId=1"
 */
export default function LegacyFrame() {
  const { "*": legacyPath } = useParams<{ "*": string }>();
  const iframeRef = useRef<HTMLIFrameElement>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  const legacySrc = `/legacy/${legacyPath ?? ""}${window.location.search}`;

  const handleLoad = useCallback(() => {
    setIsLoading(false);
    setHasError(false);
  }, []);

  const handleError = useCallback(() => {
    setIsLoading(false);
    setHasError(true);
  }, []);

  const handleRetry = useCallback(() => {
    setIsLoading(true);
    setHasError(false);
    if (iframeRef.current) {
      iframeRef.current.src = legacySrc;
    }
  }, [legacySrc]);

  if (hasError) {
    return (
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          minHeight: "calc(100vh - 60px - 56px)",
          gap: 24,
        }}
      >
        <Card
          style={{
            maxWidth: 480,
            borderRadius: 6,
            border: "1px solid var(--border-light)",
          }}
        >
          <Alert
            type="warning"
            message="Failed to load legacy page"
            description={
              <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                <Text style={{ color: "var(--text-secondary)" }}>
                  The requested legacy page could not be loaded. It may be
                  temporarily unavailable or the URL may be incorrect.
                </Text>
                <Text
                  code
                  style={{
                    fontSize: 12,
                    background: "var(--panel-muted)",
                    borderRadius: "var(--radius-md)",
                    padding: "4px 8px",
                  }}
                >
                  {legacySrc}
                </Text>
              </div>
            }
            showIcon
            style={{
              borderRadius: "var(--radius-md)",
              border: "none",
            }}
          />
        </Card>
        <Button
          type="primary"
          onClick={handleRetry}
          style={{
            background: "var(--accent)",
            borderColor: "var(--accent)",
          }}
        >
          Retry
        </Button>
      </div>
    );
  }

  return (
    <div
      style={{
        position: "relative",
        minHeight: "calc(100vh - 60px - 56px)",
      }}
    >
      {isLoading && (
        <div
          style={{
            position: "absolute",
            inset: 0,
            zIndex: 1,
          }}
        >
          <SkeletonPage />
        </div>
      )}
      <iframe
        ref={iframeRef}
        src={legacySrc}
        onLoad={handleLoad}
        onError={handleError}
        title="Legacy Page"
        style={{
          width: "100%",
          height: "calc(100vh - 60px - 56px)",
          border: "none",
          borderRadius: "var(--radius-lg)",
          background: "var(--panel)",
          opacity: isLoading ? 0 : 1,
          transition: "opacity var(--transition-base, 0.25s) ease",
        }}
        sandbox="allow-same-origin allow-scripts allow-forms allow-popups allow-modals"
      />
    </div>
  );
}
