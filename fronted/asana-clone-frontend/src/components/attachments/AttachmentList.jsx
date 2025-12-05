// src/components/attachments/AttachmentList.jsx
import { useState, useEffect } from 'react';
import { Paperclip, Upload } from 'lucide-react';
import AttachmentsItem from './AttachmentsItem';
import AttachmentsUpload from './AttachmentsUpload';
import EmptyState from '../common/EmptyState';
import Spinner from '../common/Spinner';
import Button from '../common/Button';
import attachmentService from '../../services/attachmentService';

const AttachmentList = ({ taskId }) => {
  const [attachments, setAttachments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [showUpload, setShowUpload] = useState(false);

  useEffect(() => {
    loadAttachments();
  }, [taskId]);

  const loadAttachments = async () => {
    try {
      const data = await attachmentService.getAttachmentsByTask(taskId);
      setAttachments(data);
    } catch (error) {
      console.error('Error loading attachments:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleUploadComplete = () => {
    loadAttachments();
    setShowUpload(false);
  };

  const handleDelete = async (attachmentId) => {
    if (!window.confirm('¿Estás seguro de eliminar este archivo?')) return;

    try {
      await attachmentService.deleteAttachment(taskId, attachmentId);
      setAttachments(attachments.filter(a => a.id !== attachmentId));
    } catch (error) {
      console.error('Error deleting attachment:', error);
    }
  };

  const handleDownload = async (attachmentId, fileName) => {
    try {
      await attachmentService.downloadAttachment(attachmentId, fileName);
    } catch (error) {
      console.error('Error downloading attachment:', error);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        <Spinner />
      </div>
    );
  }

  // Group attachments by type
  const images = attachments.filter(a => a.isImage);
  const documents = attachments.filter(a => !a.isImage);

  return (
    <div className="space-y-6">
      {/* Upload Button */}
      <div className="flex justify-between items-center">
        <h3 className="text-lg font-semibold text-gray-900">
          Archivos adjuntos ({attachments.length})
        </h3>
        <Button
          variant="primary"
          size="sm"
          onClick={() => setShowUpload(true)}
        >
          <Upload className="w-4 h-4 mr-2" />
          Subir archivo
        </Button>
      </div>

      {/* Upload Modal */}
      {showUpload && (
        <AttachmentsUpload
          taskId={taskId}
          onClose={() => setShowUpload(false)}
          onUploadComplete={handleUploadComplete}
        />
      )}

      {/* Empty State */}
      {attachments.length === 0 ? (
        <EmptyState
          icon={<Paperclip className="w-12 h-12" />}
          title="No hay archivos adjuntos"
          description="Sube archivos para compartir con tu equipo"
          action={
            <Button variant="primary" onClick={() => setShowUpload(true)}>
              <Upload className="w-4 h-4 mr-2" />
              Subir primer archivo
            </Button>
          }
        />
      ) : (
        <>
          {/* Images Gallery */}
          {images.length > 0 && (
            <div>
              <h4 className="text-sm font-semibold text-gray-700 mb-3">
                Imágenes ({images.length})
              </h4>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {images.map(attachment => (
                  <AttachmentsItem
                    key={attachment.id}
                    attachment={attachment}
                    onDelete={handleDelete}
                    onDownload={handleDownload}
                    viewMode="gallery"
                  />
                ))}
              </div>
            </div>
          )}

          {/* Documents List */}
          {documents.length > 0 && (
            <div>
              <h4 className="text-sm font-semibold text-gray-700 mb-3">
                Documentos ({documents.length})
              </h4>
              <div className="space-y-2">
                {documents.map(attachment => (
                  <AttachmentsItem
                    key={attachment.id}
                    attachment={attachment}
                    onDelete={handleDelete}
                    onDownload={handleDownload}
                    viewMode="list"
                  />
                ))}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default AttachmentList;