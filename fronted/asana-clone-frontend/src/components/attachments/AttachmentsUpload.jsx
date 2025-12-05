// src/components/attachments/AttachmentsUpload.jsx
import { useState, useRef } from 'react';
import { Upload, X, File, CheckCircle } from 'lucide-react';
import Button from '../common/Button';
import Modal from '../common/Modal';
import Input from '../common/Input';
import ProgressBar from '../common/ProgressBar';
import attachmentService from '../../services/attachmentService';
import { formatFileSize } from '../../utils/formatters';

const AttachmentsUpload = ({ taskId, isOpen, onClose, onUploadComplete }) => {
  const [files, setFiles] = useState([]);
  const [descriptions, setDescriptions] = useState({});
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState({});
  const fileInputRef = useRef(null);

  const handleFileSelect = (e) => {
    const selectedFiles = Array.from(e.target.files);
    setFiles([...files, ...selectedFiles]);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    const droppedFiles = Array.from(e.dataTransfer.files);
    setFiles([...files, ...droppedFiles]);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const removeFile = (index) => {
    setFiles(files.filter((_, i) => i !== index));
  };

  const updateDescription = (index, description) => {
    setDescriptions({
      ...descriptions,
      [index]: description
    });
  };

  const handleUpload = async () => {
    if (files.length === 0) return;

    setUploading(true);
    const results = [];

    for (let i = 0; i < files.length; i++) {
      try {
        setUploadProgress({ ...uploadProgress, [i]: 0 });

        const formData = new FormData();
        formData.append('file', files[i]);
        if (descriptions[i]) {
          formData.append('description', descriptions[i]);
        }

        const result = await attachmentService.uploadAttachment(
          taskId,
          formData,
          (progressEvent) => {
            const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            setUploadProgress(prev => ({ ...prev, [i]: progress }));
          }
        );

        results.push(result);
        setUploadProgress(prev => ({ ...prev, [i]: 100 }));
      } catch (error) {
        console.error(`Error uploading file ${files[i].name}:`, error);
      }
    }

    setUploading(false);
    onUploadComplete(results);
  };

  const totalSize = files.reduce((acc, file) => acc + file.size, 0);
  const maxSize = 50 * 1024 * 1024; // 50MB
  const isOverSize = totalSize > maxSize;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Subir archivos"
      size="lg"
    >
      <div className="space-y-4">
        {/* Drop Zone */}
        <div
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          onClick={() => fileInputRef.current?.click()}
          className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-indigo-500 transition-colors cursor-pointer"
        >
          <Upload className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <p className="text-gray-700 font-medium mb-1">
            Arrastra archivos aquí o haz clic para seleccionar
          </p>
          <p className="text-sm text-gray-500">
            Tamaño máximo: 50MB por archivo
          </p>
          <input
            ref={fileInputRef}
            type="file"
            multiple
            onChange={handleFileSelect}
            className="hidden"
          />
        </div>

        {/* File List */}
        {files.length > 0 && (
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h4 className="font-medium text-gray-900">
                Archivos seleccionados ({files.length})
              </h4>
              <span className={`text-sm ${isOverSize ? 'text-red-600' : 'text-gray-500'}`}>
                {formatFileSize(totalSize)} / 50MB
              </span>
            </div>

            {files.map((file, index) => (
              <div key={index} className="bg-gray-50 rounded-lg p-3">
                <div className="flex items-start gap-3">
                  <File className="w-5 h-5 text-gray-400 flex-shrink-0 mt-1" />
                  
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-2">
                      <p className="font-medium text-gray-900 truncate">
                        {file.name}
                      </p>
                      {!uploading && (
                        <button
                          onClick={() => removeFile(index)}
                          className="text-gray-400 hover:text-red-600"
                        >
                          <X className="w-4 h-4" />
                        </button>
                      )}
                      {uploadProgress[index] === 100 && (
                        <CheckCircle className="w-5 h-5 text-green-600" />
                      )}
                    </div>

                    <p className="text-sm text-gray-500 mb-2">
                      {formatFileSize(file.size)}
                    </p>

                    {uploading && uploadProgress[index] !== undefined && (
                      <ProgressBar value={uploadProgress[index]} />
                    )}

                    {!uploading && (
                      <Input
                        placeholder="Descripción (opcional)"
                        value={descriptions[index] || ''}
                        onChange={(e) => updateDescription(index, e.target.value)}
                        size="sm"
                      />
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {isOverSize && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
            El tamaño total de los archivos excede el límite de 50MB
          </div>
        )}

        {/* Actions */}
        <div className="flex justify-end gap-3 pt-4 border-t">
          <Button
            variant="secondary"
            onClick={onClose}
            disabled={uploading}
          >
            Cancelar
          </Button>
          <Button
            variant="primary"
            onClick={handleUpload}
            disabled={files.length === 0 || isOverSize || uploading}
            isLoading={uploading}
          >
            Subir {files.length} {files.length === 1 ? 'archivo' : 'archivos'}
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export default AttachmentsUpload;