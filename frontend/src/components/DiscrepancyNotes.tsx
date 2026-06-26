import { useState } from "react";
import {
  List,
  Typography,
  Tag,
  Button,
  Modal,
  Input,
  Space,
  Empty,
  Spin,
  Card,
} from "antd";

import { useEventCrfNotes, useCreateNote, useResolveNote } from "@/hooks/useDiscrepancyNotes";

const { Text, Paragraph } = Typography;
const { TextArea } = Input;

interface DiscrepancyNotesProps {
  eventCrfId: number;
  studyId: number;
  entityId: number;
}

export default function DiscrepancyNotes({ eventCrfId, studyId, entityId }: DiscrepancyNotesProps) {
  const { data: notes, isLoading } = useEventCrfNotes(eventCrfId);
  const createNote = useCreateNote();
  const resolveNote = useResolveNote();

  const [modalOpen, setModalOpen] = useState(false);
  const [description, setDescription] = useState("");
  const [detailedNotes, setDetailedNotes] = useState("");

  const handleCreate = async () => {
    if (!description.trim()) return;
    await createNote.mutateAsync({
      description: description.trim(),
      detailedNotes: detailedNotes.trim(),
      entityType: "itemData",
      entityId,
      studyId,
      eventCrfId,
      itemDataId: 0,
    });
    setDescription("");
    setDetailedNotes("");
    setModalOpen(false);
  };

  const handleResolve = async (noteId: number) => {
    await resolveNote.mutateAsync(noteId);
  };

  if (isLoading) {
    return (
      <div style={{ padding: 24, textAlign: "center" }}>
        <Spin />
      </div>
    );
  }

  return (
    <div>
      <Space style={{ width: "100%", justifyContent: "space-between", marginBottom: 16 }}>
        <Text strong>
          Discrepancy Notes ({notes?.length ?? 0})
        </Text>
        <Button
          type="primary"
          size="small"
          onClick={() => setModalOpen(true)}
        >
          Add Note
        </Button>
      </Space>

      {!notes || notes.length === 0 ? (
        <Card style={{ borderRadius: 6, background: "var(--panel-muted)" }}>
          <Empty
            description="No discrepancy notes for this CRF"
          />
        </Card>
      ) : (
        <List
          dataSource={notes}
          renderItem={(note) => (
            <List.Item
              actions={
                note.resolutionStatusId === 1 || note.resolutionStatusId === null
                  ? [
                      <Button
                        key="resolve"
                        size="small"
                        onClick={() => handleResolve(note.discrepancyNoteId)}
                        loading={resolveNote.isPending}
                      >
                        Resolve
                      </Button>,
                    ]
                  : undefined
              }
            >
              <List.Item.Meta
                title={
                  <Space>
                    <Text>{note.description}</Text>
                    <span className={`status ${note.resolutionStatusId === 5 ? "status-success" : "status-warning"}`}>
                      {note.resolutionStatusId === 5 ? "Resolved" : "New"}
                    </span>
                    <Tag>{note.discrepancyNoteTypeId ?? "Note"}</Tag>
                  </Space>
                }
                description={
                  <div>
                    {note.detailedNotes && (
                      <Paragraph
                        type="secondary"
                        style={{ marginBottom: 4, fontSize: 13 }}
                      >
                        {note.detailedNotes}
                      </Paragraph>
                    )}
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {note.dateCreated
                        ? new Date(note.dateCreated).toLocaleDateString()
                        : ""}
                    </Text>
                  </div>
                }
              />
            </List.Item>
          )}
        />
      )}

      <Modal
        title="Add Discrepancy Note"
        open={modalOpen}
        onOk={handleCreate}
        onCancel={() => setModalOpen(false)}
        confirmLoading={createNote.isPending}
        okText="Create Note"
      >
        <Space direction="vertical" style={{ width: "100%" }}>
          <div>
            <Text strong>Description</Text>
            <Input
              placeholder="Brief description of the issue"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              style={{ marginTop: 4 }}
            />
          </div>
          <div>
            <Text strong>Detailed Notes</Text>
            <TextArea
              placeholder="Additional details..."
              rows={4}
              value={detailedNotes}
              onChange={(e) => setDetailedNotes(e.target.value)}
              style={{ marginTop: 4 }}
            />
          </div>
        </Space>
      </Modal>
    </div>
  );
}
