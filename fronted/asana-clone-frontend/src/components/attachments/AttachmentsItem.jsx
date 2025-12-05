// src/components/attachments/AttachmentsItem.jsx
import { Download, Trash2, FileText, File, Image as ImageIcon } from 'lucide-react';
import Button from '../common/Button';
import { formatFileSize, formatDate } from '../../utils/formatters';

const AttachmentsItem = ({ 
  attachment, 
  onDelete, 
  onDownload, 
  viewMode = 'list' 
}) => {
  const getFileIcon = () => {
    if (attachment.isImage) return <ImageIcon className="w-5 h-5" />;
    if (attachment.mimeType?.includes('pdf')) return <FileText className="w-5 h-5" />;
    return <File className="w-5 h-5" />;
  };

  const getFileTypeColor = () => {
    if (attachment.isImage) return 'bg-purple-100 text-purple-700';
    if (attachment.mimeType?.includes('pdf')) return 'bg-red-100 text-red-700';
    if (attachment.mimeType?.includes('word')) return 'bg-blue-100 text-blue-700';
    if (attachment.mimeType?.includes('excel') || attachment.mimeType?.includes('spreadsheet')) 
      return 'bg-green-100 text-green-700';
    return 'bg-gray-100 text-gray-700';
  };

  if (viewMode === 'gallery' && attachment.isImage) {
    return (
      <div className="group relative aspect-square bg-gray-100 rounded-lg overflow-hidden">
        <img
          src={attachment.thumbnailUrl || `/api/attachments/${attachment.id}/download`}
          alt={attachment.fileName}
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-50 transition-all flex items-center justify-center gap-2">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onDownload(attachment.id, attachment.fileName)}
            className="opacity-0 group-hover:opacity-100 bg-white text-gray-900 hover:bg-gray-100"
          >
            <Download className="w-4 h-4" />
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onDelete(attachment.id)}
            className="opacity-0 group-hover:opacity-100 bg-white text-red-600 hover:bg-red-50"
          >
            <Trash2 className="w-4 h-4" />
          </Button>
        </div>
        <div className="absolute bottom-0 left-0 right-0 p-2 bg-gradient-to-t from-black to-transparent">
          <p className="text-xs text-white truncate">{attachment.fileName}</p>
        </div>
      </div>
    );
  }

  // List view
  return (
    <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors group">
      <div className={`p-2 rounded ${getFileTypeColor()}`}>
        {getFileIcon()}
      </div>

      <div className="flex-1 min-w-0">
        <p className="font-medium text-gray-900 truncate">{attachment.fileName}</p>
        <div className="flex items-center gap-3 text-sm text-gray-500">
          <span>{formatFileSize(attachment.fileSize)}</span>
          <span>•</span>
          <span>{formatDate(attachment.createdAt)}</span>
          <span>•</span>
          <span>
            {attachment.uploadedBy?.firstName} {attachment.uploadedBy?.lastName}
          </span>
          {attachment.downloadCount > 0 && (
            <>
              <span>•</span>
              <span>{attachment.downloadCount} descargas</span>
            </>
          )}
        </div>
        {attachment.description && (
          <p className="text-sm text-gray-600 mt-1">{attachment.description}</p>
        )}
      </div>

      <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
        <Button
          variant="ghost"
          size="sm"
          onClick={() => onDownload(attachment.id, attachment.fileName)}
        >
          <Download className="w-4 h-4" />
        </Button>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => onDelete(attachment.id)}
          className="text-red-600 hover:bg-red-50"
        >
          <Trash2 className="w-4 h-4" />
        </Button>
      </div>
    </div>
  );
};

export default AttachmentsItem;